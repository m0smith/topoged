(ns topoged.init
  (:import [com.tinkerpop.blueprints.impls.tg TinkerEdge TinkerVertex])
  (:require[archimedes.core :as g]
           [archimedes.vertex :as v]
           [archimedes.edge :as e]
           [topoged.db :as tdb]))

(defrecord TopogedContext [user db])

(defn- create-context []
  (g/use-clean-graph!)
  (let [db (reify tdb/DataStore
             (tdb/add-node [_ data-map] (v/create! data-map))
             
             (tdb/add-edge [_ start label end data-map] 
               (e/connect-with-id! nil start label end data-map))
             (tdb/find-by-kv [_ ky vl] (v/find-by-kv ky vl)))]
    (extend-type TinkerVertex
      tdb/DataStoreNode
      (tdb/merge-node [node data-map] (v/merge! node data-map))
      (tdb/to-data-map [node] (v/to-map node)))
    
    (extend-type TinkerEdge
      tdb/DataStoreNode
      (tdb/merge-node [node data-map] (v/merge! node data-map))
      (tdb/to-data-map [node] (v/to-map node)))
                           
    (->TopogedContext (tdb/add-node db {:type :researcher}) db)))

(defn topoged-init []
  (create-context)
  ;(db/init)
  ;(type-init)
  )

  
