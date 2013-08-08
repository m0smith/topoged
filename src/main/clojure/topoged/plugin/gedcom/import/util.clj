(ns topoged.plugin.gedcom.import.util
  (:require [topoged.data.common :as db])
  (:use [topoged.gedoverse :only (add group-agent)]))

(set! *warn-on-reflection* true)


(defn skip-handler [process-state record]
"A handler that simply ignores the input and returns the process-state unchanged."
  (println "skipping "  (:tag record))
  process-state)


(defn handle-record 
  " handler-factory is a function that accepts a GEDCOM
   tag (:HEAD, :INDI, etc) and returns a hander for that type of
   record in the current context.  A handler is a function that
   accepts process-state and record and returns a new process-state.

   default-handler is the handler used when handler-factory returns nil.

   process state is a map that contains the current state of the of the import.      :source - the source vertex for the GEDCOM being imported.  
     :path - the tags that lead to this record 
     :id-in-source - a map of GEDCOM ids to  uuid

   The record is a map of the current gedcom record with elements:
     :tag - The GEDCOM TAG as a keyword 
     :attrs - any attributes of the GEDCOM record like 
     :line-number, :representation :level 
     :value - the value of the GEDCOM record 
     :content - a seq of nested GEDCOM records
"
[handler-factory process-state 
 {:keys [tag] :as record}]
  (if-let [fun (handler-factory tag)]
    (fun process-state record)))




(defn assoc-in-process-state [doc-key dest-key 
                              process-state
                              {:keys [value] :as record}]
  
  ;(println value record dest-key (coll? dest-key))
  (let [kys (if (coll? dest-key) (mapv db/key-id dest-key) (db/key-id dest-key))]
    (assoc-in process-state [doc-key kys] value)))

(defn using-default 
"wrap the function f to return a default value when it is falsey"
[f def]
  (fn [arg]
    (if-let [rtnval (f arg)]
      rtnval
      def)))

(defn nested-handler* 
  [handlers 
   {:keys [path] :as process-state} 
   {:keys [tag content] :as record}]
  ;(println "NH*: " process-state)
  (let [ps (update-in process-state [:path] conj tag)]
    (->  (reduce (partial handle-record handlers) ps content)
         (assoc :path path))))

(defn nested-handler 
  "Created a nested process-state and then call the handlers."
  [handler-factory default-handler]
  (partial nested-handler* (using-default handler-factory default-handler)))


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

    
