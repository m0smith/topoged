(ns topoged.plugin.gedcom.import.indi
  (:require [topoged.data.inmemory :as db])
  (:use [topoged.plugin.gedcom.import.util]))
	

(defn indi-handler [sourceId record]
  (let [content (:content record )
        name (reduce #(if (= :NAME (:tag %2)) (:value %2) %1) nil content)
        persona (db/add-persona :sourceId sourceId  :name name)] 
    persona))



	  
      
      
    
