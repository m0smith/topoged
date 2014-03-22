(ns topoged.plugin.gedcom.import.indi
  (:use     
   [topoged db]
   [topoged.data path schema]
   [topoged.plugin.gedcom.import.util :only (apply-h nested-handler*)]
   ))

(defrecord IndiContext   [persona-map])

(defn assoc-in-local 
  ""
  [map-key
   attr-key 
   import-context
   {:keys [value]}
   path]
  (assoc-in import-context [:local-context map-key attr-key] value ))

(defn to-persona    [ky] (partial assoc-in-local :persona-map ky))

(defmacro to-event [prefix]
  `(partial nested-handler*
            {
             :DATE (to-persona (keyword (str (name ~prefix) "-date")))
             :PLAC (to-persona (keyword (str (name ~prefix) "-place")))
             :NOTE (to-persona (keyword (str (name ~prefix) "-note")))
             :TEMP (to-persona (keyword (str (name ~prefix) "-temple")))
             :STAT (to-persona (keyword (str (name ~prefix) "-child-status")))
             }))

(def indi-name-handler-map
  {
   :GIVN (to-persona :given-name)
   :SURN (to-persona :surname)
   :NSFX (to-persona :suffix)
   })

(def indi-handler-map
  {
   :NAME (apply-h (to-persona :name) 
                  (partial nested-handler* indi-name-handler-map))
   :AFN  (to-persona :afn)
   :SEX  (to-persona :sex)

   :BIRT (to-event :birth)
   :CHR  (to-event :christening)
   :DEAT (to-event :death)
   :BURI (to-event :burial)
   :BAPL (to-event :lds-baptism)
   :ENDL (to-event :lds-endowment)
   :SLGC (to-event :lds-sealed-to-parents)
   })



(defn indi-post-process 
  "Updates the database with the information gathered from he INDI section.
   Returns import-context with the persona added to :id-map"
  [{:keys [local-context] :as import-context} db id source user]
  (println "indi-post-process: " local-context)
  (let [{:keys [persona-map]} local-context]
    (when persona-map
      (let [i (path-create db [Individual] [persona-map])]
        (assoc-in import-context [:id-map id] {:individual i})))))
  

(defn indi-post-process-xxx
  "Updates the database with the information gathered from he INDI section.
   Returns import-context with the persona added to :id-map"
  
  [{:keys [local-context] :as import-context} db id source user]
  (let [{:keys [persona-map]} local-context
        persona (add-node db (merge persona-map {:type :persona})) 
        individual (add-node db (merge persona-map {:type :individual}))]
    (add-edge db individual :member persona {})
    (add-edge db source :contribures persona {})
    (add-edge db user :accepts individual {})
    (assoc-in import-context [:id-map id] {:persona persona :individual individual})))



(def indi-nested-handler (partial nested-handler* indi-handler-map))

(defn indi-handler 
  ""
  [{:keys [db user shared-context] :as import-context} {:keys [value] :as record} path]
  (let [local-context (->IndiContext {} )
        {:keys [source]} shared-context]
    (->  (assoc import-context :local-context local-context)
         (indi-nested-handler record path)
         (indi-post-process db value source user))))


;;(def indi-handler (partial top-level-handler top-level-handler-map))

