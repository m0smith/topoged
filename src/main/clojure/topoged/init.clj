(ns topoged.init
  (:require[archimedes.core :as g]
           [archimedes.vertex :as v]
           [archimedes.edge :as e]
           [topoged.db :as tdb]))

(defrecord TopogedContext [user db])


(defn- create-context []
  (g/use-clean-graph!)
  (let [db (reify tdb/DataStore
             (tdb/add-node [_ data-map] (v/create! data-map))
             (tdb/merge-node [_ node data-map] (v/merge! node data-map))
             (tdb/add-edge [_ start label end data-map] 
               (e/connect-with-id! nil start label end data-map)))]
    (->TopogedContext (tdb/add-node db {:type :researcher}) db)))

(defn topoged-init []
  (create-context)
  ;(db/init)
  ;(type-init)
  )

  
