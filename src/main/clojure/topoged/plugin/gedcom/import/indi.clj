(ns topoged.plugin.gedcom.import.indi
  (:use [topoged.gedoverse :only (add persona-agent)]
	[topoged.util :only (apply-symbol)]
	[topoged.plugin.gedcom.import.util]
	))

(set! *warn-on-reflection* true)


(defstruct persona-struct  :id :source :idInSource :name )

(defn indi-handler [uuid record status-agent]
  (println "INDI:" record)
  (let [id (source-id record)
	content (:content record )
	persona (struct persona-struct
			(new-id uuid record) uuid
			id
			(first (map source-id
				    (filter #(= (:tag %) :NAME) content))))]
      (send persona-agent add-persona persona status-agent)))


	  
      
      
    
