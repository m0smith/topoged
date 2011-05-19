(ns topoged.plugin.gedcom.import.fam
  (:use [topoged.gedoverse :only (add group-agent)]
	[topoged.util :only (apply-symbol)]
	[topoged.plugin.gedcom.import.util]
	))

(set! *warn-on-reflection* true)

(defmulti record-handler (fn [_ rec __] (:tag rec))) 

(defmethod record-handler :HUSB [uuid record group]
	   (let [id (new-id uuid record)]
	     (update-in (assoc group :father id)
			[:parents] assoc 0 id)))


(defmethod record-handler :WIFE [uuid record group]
	   (let [id (new-id uuid record)]
	     (update-in (assoc group :mother id)
			[:parents] assoc 1 id)))

(defmethod record-handler :CHIL [uuid record group]
	   (let [id (new-id uuid record)]
	     (update-in group [:children] conj id)))

(defmethod record-handler :default [uuid record group] group)

(defstruct group-struct :type :id :source :idInSource :parents :children)


(defn fam-handler [uuid record status-agent]
  "status-agent [message indi-count fam-count sour-count]"
  (loop [coll (:content record)
	 group (struct group-struct :family (new-id uuid record)
		       uuid (source-id record)
		       [nil nil] [])]

    (if (seq coll)
      (let [f (first coll)
	    new-group (record-handler uuid f group)]
	(recur (rest coll) (if new-group new-group group)))
      (do
	(send group-agent add-group group status-agent)))))
	  
      
      
    
