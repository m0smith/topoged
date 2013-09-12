(ns topoged.db
 ; (:require [archimedes.edge :as e]))
)

;(def add-edge (partial e/connect-with-id! nil))

(defprotocol DataStore
  (add-node [db data-map])
  (add-edge [db start label end data-map])
  (find-by-kv [db ky vl]))

(defprotocol DataStoreNode
  (to-data-map [node])
  (merge-node [node data-map])
  )

