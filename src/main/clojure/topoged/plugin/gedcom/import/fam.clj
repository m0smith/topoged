(ns topoged.plugin.gedcom.import.fam
  (:require [archimedes.vertex :as v])
  (:use     [topoged.plugin.gedcom.import.util]
            [topoged db]))

(defn to-persona [k] (assoc-2nd-handler :persona k))

(defn assoc-in-fam-nested 
  "Assoc the current value in the 2nd state, then pass it to the handlers to process
the nested values"
[doc-key dest-keys handlers]
  (fn [ps record path hs & more]
    (let [r1 (apply assoc-in-second-state doc-key dest-keys ps record path hs more)]
      (apply (nested-handler handlers skip-handler)
             (process-state r1) record path (other-states r1)))))

(defmacro to-event [prefix]
  `(nested-handler 
    {
     :DATE (to-persona (keyword (str (name ~prefix) "-date")))
     :PLAC (to-persona (keyword (str (name ~prefix) "-place")))
     :NOTE (to-persona (keyword (str (name ~prefix) "-note")))
     :TEMP (to-persona (keyword (str (name ~prefix) "-temple")))
     :STAT (to-persona (keyword (str (name ~prefix) "-child-status")))
     }
    skip-handler))


(def top-level-handler-map
{
 :NAME (assoc-in-fam-nested :persona :name (nested-handler name-handler-map skip-handler))
 :AFN (to-persona :afn)
 :SEX (to-persona :gender)
 :BIRT (to-event :birth)
 :CHR (to-event :christening)
 :DEAT (to-event :death)
 :BURI (to-event :burial)
 :BAPL (to-event :lds-baptism)
 :ENDL (to-event :lds-endowment)
 :SLGC (to-event :lds-sealed-to-parents)
})
   
(defn add-vertex [m]
  (v/create! m))
   
(defn- top-level-handler [handlers ps {:keys [tag value] :as record} path & more]
  (let [subhandler (nested-handler handlers skip-handler)
        [new-ps state-2 :as rtnval] (apply subhandler ps record path {:fam-state 1 :child-index 0} more) 
        ]
    ;(println "top-level-handler <<" state-2 new-ps value)
    (let [persona-v (add-vertex (merge (:persona state-2) {:type :persona }))
          famvidual-v (add-vertex (merge (:persona state-2) {:type :famvidual }))]
      (add-edge famvidual-v :member persona-v)
      (add-edge (:source new-ps) :contributes persona-v)
      (add-edge *researcher* :accepts famvidual-v)
      
      (-> new-ps
         (assoc-in [:id-map value] famvidual-v)) 
)))

(def fam-handler (partial top-level-handler top-level-handler-map))

