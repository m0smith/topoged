(ns topoged.plugin.gedcom.import.util
  (:require [topoged.data.common :as db])
  (:use [topoged.gedoverse :only (add group-agent)]))

(set! *warn-on-reflection* true)


(defn skip-handler [process-state record]
  (println "skipping " (:path process-state) (:tag record))
  process-state)


(defn handle-record [handlers default-handler process-state {:keys [tag] :as record}]
  "The process state is a map that contains the current state of the if the import.
    :source - the source that represents the GEDCOM being imported.
    :path - the tags that lead to this record
    :id-in-source - a map of GEDCOM ids to uuid

   The record is a map of the current gedcom record with elements:
     :tag - The GEDCOM TAG as a keyword
     :attrs - any attributes of the GEDCOM record like :line-number, :representation :level
     :value - the value of the GEDCOM record
     :content - a seq of nested GEDCOM records
    "

  (let [fun (handlers tag)
        fun (if-not fun default-handler fun)]
    (fun process-state record)))


(defn assoc-in-process-state [doc-key dest-key 
                              process-state
                              {:keys [value] :as record}]
  ;(println value record dest-key (coll? dest-key))
  (let [kys (if (coll? dest-key) (mapv db/key-id dest-key) (db/key-id dest-key))]
    (assoc-in process-state [doc-key kys] value)))


(defn nested-handler* 
  [handlers def 
   {:keys [path] :as process-state} 
   {:keys [tag content] :as record}]
  ;(println "NH*: " process-state)
  (let [ps (update-in process-state [:path] conj tag)]
    (->  (reduce (partial handle-record handlers def) ps content)
         (assoc :path path))))

(defn nested-handler [handlers def]
  (partial nested-handler* handlers def))


(defn thread-process-state [f1 f2 process-state record]
  (-> process-state (f1 record) (f2 record)))

(def counter-map { :persona 1 :group 2 :source 3 })

(defn status-importing [m key]
  "Update the status message and increment the counter"
  (let [index (counter-map key)]
    (assoc m 0 "Importing" index (inc (m index))))) 


(defn add-record [map rec status-agent type]
  (send status-agent status-importing type)
  (add map rec))

(defn add-persona [map rec status-agent] (add-record map rec status-agent :persona))
(defn add-group [map rec status-agent] (add-record map rec status-agent :group))
(defn add-source [map rec status-agent] (add-record map rec status-agent :source))


(defn source-id [record] (-> record :attrs :value))

(defn new-id [uuid record] (str uuid "-" (source-id record)))

    
