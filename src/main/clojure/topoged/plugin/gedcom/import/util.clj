(ns topoged.plugin.gedcom.import.util
  (:use [topoged.gedoverse :only (add group-agent)]))

(set! *warn-on-reflection* true)

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

    