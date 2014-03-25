(ns topoged.init
  (:import [com.tinkerpop.blueprints.impls.tg TinkerEdge TinkerVertex])
  (:require[archimedes.core :as g]
           [archimedes.vertex :as v]
           [archimedes.edge :as e]
           [topoged.db :as tdb]))

(defrecord TopogedContext [user db locale])

(defn- create-context []
  (g/use-clean-graph!)
  (let [db (reify tdb/DataStore
             (tdb/add-node [_ data-map] (v/create! data-map))
             (tdb/jung [_] g/*graph*)
             
             (tdb/add-edge [_ start label end data-map] 
               (e/connect-with-id! nil start label end data-map))
             (tdb/find-by-kv [_ ky vl] (v/find-by-kv ky vl)))]
                           
    (->TopogedContext (tdb/add-node db {:type :researcher}) db :es)))

(defn topoged-init []
  (create-context)
  ;(db/init)
  ;(type-init)
  )

  
