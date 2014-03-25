(ns topoged.db

)



(defprotocol DataStore
  (add-node [db data-map])
  (add-edge [db start label end data-map])
  (find-by-kv [db ky vl])
  (jung [db] ))


