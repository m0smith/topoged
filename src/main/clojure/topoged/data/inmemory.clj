(ns topoged.data.inmemory
  (:import [java.io FileNotFoundException])
  (:use [clojure.core.logic :rename {== ?==}])

 )

;;
;; To implement a new data namespace, it is only necessary to implement the following the
;; functions that reference "data-store", namely:
;;   init              - called before any other call to allow the data store to get its act
;;                       together
;;   shutdown          - called to allow the database to free resources
;;   dbsync              - cause the data store to sync (whatever that means for the data store
;;   add-to-data-store - this function accepts the given map and adds it to the data store.
;;   indatastoreo      - expects the data-store to look like a seq.  This could be done by
;;                       pre-searching the database or if using Datomic see
;;        https://github.com/clojure/core.logic/wiki/Extending-core.logic-%28Datomic-example%29



;; Database Specific stuff

(in-ns 'topoged.data.common)

(def data-store (atom []))
(def index-by-id (atom {}))

(def dir (str (System/getProperty "user.home") "/.topoged/"))
(def filename (str dir "entity.edn"))
(def index-filename (str dir "index.edn"))

(defn save-db [db filename]
  (println "save db" filename)
  (println (.mkdirs (java.io.File. dir)))
  (spit 
   filename 
   (with-out-str (prn db))))

(defn load-db [filename]
  (with-in-str (slurp filename)
    (read)))
  
(defn dbsync []
  (save-db @data-store filename)
  (save-db @index-by-id index-filename))

(defn init []
  (try
    (reset! data-store (load-db filename ))
    (reset! index-by-id (load-db index-filename ))
    (catch java.io.FileNotFoundException _)))
  

(defn shutdown [] (dbsync))

(defn by-indexo [k v]
  (let [id (@index-by-id k)] 
    (if id
      (?== v id)
      fail)))

(defn indatastoreo [pm m]
  (let [k (:id m)
        v (@index-by-id k)
        ds (if v [v] @data-store)]
    ;;(println "DATASTORE" (count ds) m k v)
    (membero pm @data-store)))

(defn add-to-data-store [m]
  (swap! data-store conj m)
  (when-let [id (:id m)] (swap! index-by-id assoc id m)))
