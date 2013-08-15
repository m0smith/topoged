(ns topoged.plugin.gedcom.import.head
  (:use [topoged.plugin.gedcom.import.util]
        [topoged db])
  (:require             [archimedes.vertex :as v]))


(defn assoc-in-head-state [doc-key dest-keys ps {:keys [value] :as record} path hs & more]
  ;(println "assoc-in-head-state " doc-key dest-keys value path hs)
  [ps (assoc-in hs [doc-key dest-keys] value)])


(defn assoc-in-source [k]
  (partial assoc-in-process-state :source k))

(defn assoc-in-source-nested [k handlers def]
  (partial thread-process-state (assoc-in-source k) (nested-handler handlers def)))

(def gedc-handlers
{
 :VERS (partial assoc-in-head-state :media :version)
 :FORM (partial assoc-in-head-state :media :form)
})

(defn assoc-in-head-nested [doc-key dest-keys handlers]
  (fn [ps record path hs & more]
    (let [r1 (apply assoc-in-head-state doc-key dest-keys ps record path hs more)]
      (apply (nested-handler handlers skip-handler)
             (process-state r1) record path (other-states r1)))))


(def sour-handlers
{
 :VERS (partial assoc-in-head-state :publisher :version)
 :NAME (partial assoc-in-head-state :source :publisher)
 :CORP (partial assoc-in-head-nested :publisher :corporation
                {:ADDR (partial assoc-in-head-state :publisher :address)})
 :DATA (nested-handler 
        {
         :DATE (partial assoc-in-head-state :source :date)
         :COPR (partial assoc-in-head-state :source :copyright)
         } skip-handler)})


(def head-handlers
{
 :SOUR (assoc-in-head-nested :source :publisher sour-handlers)
 :DATE (partial assoc-in-head-state :source :date)
 :FILE (partial assoc-in-head-state :source :title)
 :LANG (partial assoc-in-head-state :source :language)
 :GEDC (nested-handler gedc-handlers skip-handler)
 :DEST (partial assoc-in-head-state :media :destination)
 :CHAR (partial assoc-in-head-state :media :charset)
})

(defn ensure-vector [ps head-state ky]
  (if-let [ m (ky head-state)]
    (let [vector (ky ps)]
      (if vector
        (do (v/merge! vector m) 
            ps)
        (let [vector (v/create! m)
              source (:source ps)]
          (println "ensure-vector" ps head-state ky vector)
          (add-edge source ky vector)
          (assoc-in ps [ky] vector))))
    ps))

      
(defn top-level-head-handler [handlers ps {:keys [tag] :as record} path & more]
  (let [subhandler (nested-handler handlers skip-handler)
        [new-ps head-state :as rtnval] (apply subhandler ps record path {:head-state 1} more) 
        ]
    (println "top-level-head-handler" rtnval )
    (-> new-ps
        (ensure-vector head-state :source) 
        (ensure-vector head-state :media) 
        (ensure-vector head-state :repository) 
        (ensure-vector head-state :publisher))))

(def head-handler (partial top-level-head-handler head-handlers))

(def subm-handler 
  (partial top-level-head-handler 
   {
    :NAME (partial assoc-in-head-state :source :author)
    :ADDR (partial assoc-in-head-state :repository :address)
    :COMM (partial assoc-in-head-state :repository :notes)
    :PHONE (partial assoc-in-head-state :repository :phone)
    :PHON (partial assoc-in-head-state :repository :phone)
    :EMAIL (partial assoc-in-head-state :repository :email)
    :_EMAIL (partial assoc-in-head-state :repository :email)
    :CTRY (partial assoc-in-head-state :repository :country)
    :DEST (partial assoc-in-head-state :media :destination)
    } 
   ))
    
