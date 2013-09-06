(ns topoged.plugin.gedcom.import.head
  (:use     
   [topoged db]
   [topoged.plugin.gedcom.import.util :only (apply-h nested-handler*)]
   ))


(defrecord HeadContext   [source-map media-map publisher-map repository-map])


(defn head-rec-handler [map-key
                        attr-key 
                        import-context
                        {:keys [value]}
                        path]
  (assoc-in import-context [:local-context map-key attr-key] value ))


(defn to-source    [ky] (partial head-rec-handler :source-map ky))
(defn to-media     [ky] (partial head-rec-handler :media-map ky))
(defn to-publisher [ky] (partial head-rec-handler :publisher-map ky))
(defn to-repository [ky] (partial head-rec-handler :repository-map ky))

(def gedc-handler-map
{
 :VERS (to-media :version)
 :FORM (to-media :form)
 })


(def sour-handler-map
{
 :VERS (to-publisher :version)
 :NAME (to-source :publisher)
 :CORP (apply-h (to-publisher :corporation)
                (partial nested-handler* 
                         {:ADDR (to-publisher :address)}))
 :DATA (partial nested-handler*
                {
                 :DATE (to-source :date)
                 :COPR (to-source :copyright)
                 })})


(def head-handler-map 
  {:FILE (to-source :title)
   :DATE (to-source :date) 
   :LANG (to-source :language)
   :DEST (to-media  :destination)
   :CHAR (to-media  :charset)
   :GEDC (partial nested-handler* gedc-handler-map)
   :SOUR (apply-h (to-source :publisher) 
                  (to-publisher :name)
                  (partial nested-handler* sour-handler-map))
   }
)

(def nested-head-handler (partial nested-handler* head-handler-map))


(defn add-node-when [attr-map db]
  (when (> (count attr-map) 0)
    (add-node db attr-map)))

(defn merge-node-when [attr-map db node]
  (when (> (count attr-map) 0)
    (merge-node db node attr-map)))

(defn add-edge-when [in db label out]
  (when (> (count in) 0)
    (add-edge db out label in {})))

(defn head-post-process 
"Updates the database with the information gathered from he HEAD section.
Returns a vector of [publisher-vertex repository-vertex] either of which can be nil."
[db {:keys [source-map media-map repository-map publisher-map]}  source media]
(merge-node-when source-map db source)
(merge-node-when media-map db media)
(let [pub (add-node-when publisher-map db) 
      repo (add-node-when repository-map db)]
  (add-edge-when pub db :publisher source)
  (add-edge-when repo db :repository source)
  [pub repo]))

(defn head-handler2 [{:keys [db shared-context] :as import-context}
                     record path]
  (let [local-context (->HeadContext {} {} {} {} )
        {:keys [source media]} shared-context
        rtnval  (->  (assoc import-context :local-context local-context)
                     (nested-head-handler record path))]
    (head-post-process db local-context source media)
    rtnval))

(def subm-handler-map
  {
    :NAME (to-source :author)
    :ADDR (to-repository :address)
    :COMM (to-repository :notes)
    :PHONE (to-repository :phone)
    :PHON (to-repository :phone)
    :EMAIL (to-repository :email)
    :_EMAIL (to-repository :email)
    :CTRY (to-repository :country)
    :DEST (to-media :destination)
    } 

  )

(def subm-nested-handler (partial nested-handler* subm-handler-map))

(defn subm-handler [{:keys [db shared-context] :as import-context}
                    record path]
  (let [local-context (->HeadContext {} {} {} {} )
        {:keys [source media]} shared-context
        rtnval  (->  (assoc import-context :local-context local-context)
                     (subm-nested-handler record path))]
    (head-post-process db local-context source media)
    rtnval))

    
    

