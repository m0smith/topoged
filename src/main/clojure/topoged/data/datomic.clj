(ns topoged.data.datomic)
(use '[datomic.api :as d])
(use 'clojure.pprint)

(def uri "datomic:free://localhost:4334/topoged")
(d/create-database uri)

(defn- connect (d/connect uri))
