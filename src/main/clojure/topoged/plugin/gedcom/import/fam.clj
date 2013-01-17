(ns topoged.plugin.gedcom.import.fam
    (:require [topoged.data.inmemory :as db])
    (:use [topoged.plugin.gedcom.import.util]))

(defmulti fam-record-handler (fn [_ rec __] (:tag rec))) 

(defmethod fam-record-handler :HUSB [process-state record group]
  (println "HUSB" record)
  (let [{:keys [value]} record
        id (get process-state value)]
    (update-in (assoc group :father id)
               [:parents 0] assoc 1 id)))

(defmethod fam-record-handler :WIFE [process-state record group]
  (let [{:keys [value]} record
        id (get process-state value)]
    (update-in (assoc group :mother id)
               [:parents 1] assoc 1 id)))

(defmethod fam-record-handler :CHIL [process-state record group]
  (let [{:keys [value]} record
        id (get process-state value)]
    (update-in group [:children] conj id)))

(defmethod fam-record-handler :default [process-state record group] group)

(defn fam-handler [sourceId process-state record]
  ""
  (loop [coll (:content record)
         group (db/init-group sourceId)]
    (println "FAM:" record (count coll))    
    (if (seq coll)
      (let [[f & r] coll
            new-group (fam-record-handler process-state f group)]
        (recur r (if new-group new-group group)))
      (do
        (println "GROUP : " group)
        (db/add-lineage-group group)
        process-state))))

      
      
    
