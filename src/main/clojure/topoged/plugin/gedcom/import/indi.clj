(ns topoged.plugin.gedcom.import.indi
  (:require [archimedes.vertex :as v])
  (:use     [topoged.plugin.gedcom.import.util]
            [topoged db]))

(defn to-persona [k] (assoc-2nd-handler :persona k))

(defn assoc-in-indi-nested 
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

(def name-handler-map
{
 :GIVN (to-persona :given-name)
 :SURN (to-persona :surname)
 :NSFX (to-persona :suffix)
})

(def top-level-handler-map
{
 :NAME (assoc-in-indi-nested :persona :name (nested-handler name-handler-map skip-handler))
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
   
(defn- ensure-vertex2
  "Make sure the vertex exists and add to the process-state (ps).  
Also create an edge to the source
Returnthe process-state."
  [ps head-state ky]
  (if-let [ m (ky head-state)]
    (let [vertex (ky ps)]
      (if vertex
        (do (v/merge! vertex m) 
            ps)
        (let [vertex (v/create! m)
              source (:source ps)]
          ;(println "ensure-vertex" ps head-state ky vertex)
          (add-edge source ky vertex)
          (assoc-in ps [ky] vertex))))
    ps))

(defn add-vertex [m]
  (v/create! m))
   
(defn- top-level-handler [handlers ps {:keys [tag value] :as record} path & more]
  ;(println "top-level-handler >>" ps)
  (let [subhandler (nested-handler handlers skip-handler)
        [new-ps state-2 :as rtnval] (apply subhandler ps record path {:indi-state 1} more) 
        ]
    (println "top-level-handler <<" state-2 new-ps value)
    (let [persona-v (add-vertex (merge (:persona state-2) {:type :persona }))
          individual-v (add-vertex (merge (:persona state-2) {:type :individual }))]
      (add-edge individual-v :member persona-v)
      (add-edge (:source new-ps) :contributes persona-v)
      (add-edge *researcher* :accepts individual-v)
      
      (-> new-ps
         (assoc-in [:id-map value] {:persona persona-v :individual individual-v})) 
)))

(def indi-handler (partial top-level-handler top-level-handler-map))

