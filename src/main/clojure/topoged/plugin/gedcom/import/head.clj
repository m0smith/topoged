(ns topoged.plugin.gedcom.import.head
  (:use [topoged.plugin.gedcom.import.util]))


(defn assoc-in-source [k]
  (partial assoc-in-process-state :source k))

(defn assoc-in-source-nested [k handlers def]
  (partial thread-process-state (assoc-in-source k) (nested-handler handlers def)))

(def gedc-handlers
{
 :VERS (assoc-in-source [:gedcom :version])
 :FORM (assoc-in-source [:gedcom :form])
})

(def sour-handlers
{
 :VERS (assoc-in-source [:publisher :version])
 :NAME (assoc-in-source :publisher)
 :CORP (assoc-in-source-nested [:publisher :corporation]
                               {:ADDR (assoc-in-source [:publisher :address])} 
                               skip-handler)
 :DATA (nested-handler 
        {
         :DATE (assoc-in-source :date)
         :COPR (assoc-in-source :copyright)
         } skip-handler)
 
})

(def head-handlers
{
 :SOUR (assoc-in-source-nested :publisher sour-handlers skip-handler)
 :DATE (assoc-in-source :date)
 :FILE (assoc-in-source :title)
 :CHAR (assoc-in-source :characterSet)
 :LANG (assoc-in-source :language)
 :GEDC (nested-handler gedc-handlers skip-handler)
 :DEST (assoc-in-source [:gedcom :destination])
})
      
(def head-handler (nested-handler head-handlers skip-handler))

(def subm-handler 
  (nested-handler 
   {
    :NAME (assoc-in-source :author)
    :ADDR (assoc-in-source :address)
    :COMM (assoc-in-source :notes)
    :PHONE (assoc-in-source :phone)
    :PHON (assoc-in-source :phone)
    :EMAIL (assoc-in-source :email)
    :_EMAIL (assoc-in-source :email)
    :CTRY (assoc-in-source :country)
    :DEST (assoc-in-source [:gedcom :destination])
    } 
   skip-handler))
    
