(ns monocular.core
  (:require [monocular.data-map :refer [map->grammar map->transforms]]
            [instaparse.core :as insta]
            [instaparse.combinators :refer [ebnf]]
            [datomic.api :as d]))

(def base-grammar
  (ebnf
    "search = term (<whitespace> term)*
     default = value
     <value> = quoted-value | raw-value
     <raw-value> = #'[^\":\\s]+'
     <quoted-value> = <'\"'> #'[^\"]*' <'\"'>
     whitespace = #'\\s+'"))

;; We may want to have the option to change this. For our original idea of the
;; transform functions taking a Datomic and returning a Datomic query, this is
;; what we want, but if you're doing SQL, you may want your transform functions
;; to return strings that are put together afterward.
(def base-transforms
  {:search comp})

(defn search
  [searcher string]
  (->> string
      (insta/parse (:parser searcher))
      (insta/transform (:transforms searcher))))

(defrecord Searcher [parser transforms]
  clojure.lang.IFn
  (invoke [searcher string] (search searcher string))
  (applyTo [searcher args] (apply search searcher args)))

(defn searcher
  [data-map]
  (Searcher. (insta/parser (merge base-grammar (map->grammar data-map)) :start :search)
             (merge base-transforms (map->transforms data-map))))

;; Is this a good way to do this? It seems something like this is worth having.
;; It seems likely that most cases won't need to perform searches on more than
;; one set of data, and this makes that easy.
;;
;; todo: add doc string
(defmacro defsearch
  [name data-map data-set]
  (let [searchersym (gensym name)]
    `(do (def ~searchersym (searcher ~data-map))
         (defn ~name [search#] ((~searchersym search#) ~data-set)))))

(def default-query
  '{:find [?e]
    :in [$ % ?search]
    :where []})

(defmacro defsearch-datomic
  ([fname data-map conn]
   (defsearch-datomic fname data-map conn '[] default-query))
  ([fname data-map conn rules]
   (defsearch-datomic fname data-map conn rules default-query))
  ([fname data-map conn rules base-query]
   (let [searchersym (gensym fname)]
     `(do (def ~searchersym (searcher ~data-map))
          (defn ~fname [search#] (d/q ((~searchersym search#) ~base-query) ~conn ~rules))))))
