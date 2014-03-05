(ns monocular.datomic
  (:require [datomic.api :as d]))

(defn query-vector->map
  "Convert a vector datomic query to a map one to make it easier to work with"
  [query-vector]
  
  )

;; todo: come up with better names
(defn query->function
  "Returns a function suitable to be used in the data map"
  (fn generated-query-fn [] ) [query])
