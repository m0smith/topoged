(ns topoged.type
  (:require [topoged.data.common :as db])
  (:use [topoged.type.wellknown]
        [slingshot.slingshot :only [throw+]]))






(defn ensure-well-known-type [[ type-key type-id desc representation-name representation-id]]
  (println "ensure-well-known-type:" type-id)
  (let [m {:id type-id}
        entities (db/entities m)
        {:keys [id key] :as entity} (first entities)]
    (println "ensure-well-known-type:" (class key) (class type-key))
    (cond
     (nil? id) (db/add-entity [] type-id 
                              [:id type-id
                               :key type-key 
                               :docType type-document-id 
                               :description desc])
     (not= key type-key) (throw+ :type ::invalid-well-known-type 
                                 :entity entity 
                                 :type-id type-id 
                                 :type-key type-key
                                 :key key))
    )
  (let [m {:id representation-id}
        {:keys [id value] :as entity} (first (db/entities m))]
    (cond


     (nil? id) (db/add-entity [] representation-id [:id representation-id
                                                    :docType representation-document-id 
                                                    :context default-context-id
                                                    :value representation-name
                                                    :typeKey type-key
                                                    :typeId type-id])




     (not= value representation-name) (throw+ {:type ::invalid-well-known-type
                                              :entity entity
                                              :value value
                                              :id representation-id
                                              :representation-name representation-name}))))



(defn type-init []
  (println "type-init")
  (doseq [well-known-type well-known-types] (ensure-well-known-type well-known-type)))