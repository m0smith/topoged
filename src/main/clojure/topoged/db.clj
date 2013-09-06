(ns topoged.db
 ; (:require [archimedes.edge :as e]))
)

;(def add-edge (partial e/connect-with-id! nil))

(defprotocol DataStore
  (add-node [db data-map])
  (merge-node [db node data-map])
  (add-edge [db start label end data-map]))

