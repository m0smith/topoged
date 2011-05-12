(ns topoged.plugin.gedcom.import.util)

(set! *warn-on-reflection* true)

(def counter-map { :persona 1 :group 2 :source 3 })

(defn status-importing [m key]
  "Update the status message and increment the counter"
  (let [index (counter-map key)]
    (assoc m 0 "Importing" index (inc (m index))))) 
	 

(defn source-id [record] (-> record :attrs :value))

(defn new-id [uuid record] (str uuid "-" (source-id record)))

    
