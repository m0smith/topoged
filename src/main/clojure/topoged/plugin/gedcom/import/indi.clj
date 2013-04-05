(ns topoged.plugin.gedcom.import.indi
  (:require [topoged.data.common :as db])
  (:use [topoged.plugin.gedcom.import.util]
        [topoged.util :only (uuid)]))



(defn new-gedcom-persona [{:keys [id] :as source} id-in-source]
  {
   :id (uuid)
   :key :persona-document
   :docType (db/key-id :persona-document)
   :sourceId id
   (db/key-id :id-in-source) id-in-source
   }
  )

(defn new-gedcom-group [{:keys [id] :as source} type id-in-source persona role]
  {
   :id (uuid)
   :key :event-group-document
   :docType (db/key-id :event-group-document)
   :sourceId id
   :type (db/key-id type)
   (db/key-id :id-in-source) id-in-source
   :members [:member (:id persona ) :role (db/key-id role) :order 0]
   }
  )

(defn new-gedcom-attribute [{:keys [id]} type value]
 {
  :id (uuid)
  :key :attribute-document
  :docType (db/key-id :attribute-document)
  :type (db/key-id type)
  :owner id
  :value value
  
   }
)

(defn new-gedcom-individual []
 {
  :id (uuid)
  :key :individual-document
  :docType (db/key-id :individual-document)
  :attributes []
  :children []
  :parents []
  :groups []
   })

(defn new-gedcom-conclusion [{:keys [id name] :as researcher} individual persona]
 {
  :id (uuid)
  :key :conclusion-document
  :docType (db/key-id :conclusion-document)
  :owner (:id individual)
  :member (:id persona)
  :researcher id
  :rationale name
  :basis [(:id persona)]
  })
  
(defn add-persona-to-db [{:keys [persona ps]}]
  (db/add-entity db/persona-keys db/persona-type persona)
  ps)

(defn add-individual-to-db [{:keys [individual] :as ps}]
  (db/add-entity db/individual-keys db/individual-type individual)
  ps)

(defn add-conclusion-to-db [{:keys [individual persona researcher] :as ps}]
  (let [ conc (new-gedcom-conclusion researcher persona individual)]
    (db/add-entity db/conclusion-keys db/conclusion-type conc)
    ps))

(defn add-attribute-to-db [attr-type {:keys [persona] :as ps} {:keys [value]}]
  (let [attr (new-gedcom-attribute persona attr-type value)]
    (db/add-entity db/attribute-keys db/attribute-type attr)
    (update-in ps [:individual :attributes] conj (:id attr))))

(defn add-group-to-db [{:keys [group] :as ps}]
  (db/add-entity db/group-keys db/group-type group)
  (update-in ps [:individual :groups] conj (:id group)))

(defn assoc-in-persona [k]
  (fn [ps r]
    (let [new-ps (if (= k :name ) (assoc-in-process-state :persona k ps r) ps)]
      (add-attribute-to-db k new-ps r))))

(defn assoc-in-persona-nested [k handlers def]
  (partial thread-process-state (assoc-in-persona k) (nested-handler handlers def)))

(defn group-handler [source persona type role]
  (fn [ps {:keys [value] :as rec}]
    (let [g (new-gedcom-group source type value persona role)
          new-ps (assoc ps :group g)]
      (-> (nested-handler* 
           {
            :DATE (partial assoc-in-process-state :group :date)
            :PLAC (partial assoc-in-process-state :group :place)
            :NOTE (partial assoc-in-process-state :group :notes)
            :STAT (partial assoc-in-process-state :group :gedcom-status)
            :TEMP (partial assoc-in-process-state :group :temple)
            } skip-handler new-ps rec)
          add-group-to-db
          (dissoc :group)))))
      

(defn indi-handler [{:keys [source researcher] :as process-state} 
                    {:keys [value]  :as record}]
  (let [{:keys [id] :as persona} (new-gedcom-persona source value)
        individual (new-gedcom-individual)
        ps (update-in process-state [:id-in-source] assoc value id)]
    (-> (nested-handler*
         {
          :NAME (assoc-in-persona-nested 
                 :name 
                 {
                  :GIVN (assoc-in-persona :given-name)
                  :SURN (assoc-in-persona :surname)
                  :NSFX (assoc-in-persona :suffix)
                  } skip-handler)
          :TITL (assoc-in-persona :title)
          :SEX  (assoc-in-persona :gender)
          :AFN  (assoc-in-persona :ancestral-file-number)
          :BIRT (group-handler source persona :birth :child)
          :CHR  (group-handler source persona :christening :baptismal-candidate)
          :DEAT (group-handler source persona :death :deceased)
          :BURI (group-handler source persona :death :deceased)
          :BAPL (group-handler source persona :lds-baptism  :baptismal-candidate)
          :ENDL (group-handler source persona :lds-endowment  :recipient)
          :SLGC (group-handler source persona :lds-sealed-to-parents :child)
          } skip-handler 
            {:persona persona :ps ps :individual individual :researcher researcher} 
            record)
        add-individual-to-db
        add-conclusion-to-db
        add-persona-to-db
        )))
    



	  
      
      
    
