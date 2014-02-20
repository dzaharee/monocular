(ns monocular.examples.datomic
  ^{:doc "Example showing how to use Monocular with Datomic"}
  (:require [monocular.core :as monocular]
            [datomic.api :as d]))

(def uri "datomic:mem://monocular-demo")

(def schema
  [{:db/id #db/id [:db.part/db]
    :db/ident :doctor
    :db/valueType :db.type/string
    :db/cardinality :db.cardinality/one




    }]
  )

(def data)

(defn init-db!
  []
  (d/create-database uri)
  (let [conn (d/connect uri)]
    (d/transact conn schema)
    (d/transact conn data)
    conn))

(defn reset-db!
  []
  (d/delete-database uri)
  (init-db!))


