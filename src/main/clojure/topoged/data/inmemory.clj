(ns topoged.data.inmemory
  (:import [java.io FileNotFoundException])
  (:use [clojure.core.logic :rename {== ?==}])
  (:use [clojure.core.atom]))

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


(def filename (str (System/getProperty "user.home") "/topoged.tgd"))

(defn save-db [db filename]
  (spit 
   filename 
   (with-out-str (prn @data-store))))

(defn load-db [filename]
  
  (with-in-str (slurp filename)
    (read)))
  
(defn dbsync []
  (save-db @data-store filename))

(defn init []
  (try
    (reset! data-store (load-db filename ))
    (catch java.io.FileNotFoundException _)))
  

(defn shutdown [] (dbsync))

(defn indatastoreo [pm]
  (membero pm @data-store))

(defn add-to-data-store [m]
  (swap! data-store conj m))
