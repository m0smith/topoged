(ns topoged.plugin.gedcom.import.fam
  (:require [archimedes.vertex :as v])
  (:use     [topoged.plugin.gedcom.import.util]
            [topoged db]))

(defrecord FamContext   [group-map parents children child-index])


(defn assoc-in-local 
  ""
  [map-key   attr-key    import-context   {:keys [value]}   path]
  (assoc-in import-context [:local-context map-key attr-key] value ))

(defn to-group    [ky] (partial assoc-in-local :group-map ky))

(defn to-parents
  ([type order import-context {:keys [value]} path]
     (update-in import-context [:local-context :parents] conj
                {:order order :type type :value value})))

(defn to-children
  ([type order import-context {:keys [value]} path]
     (update-in import-context [:local-context :children] conj
                {:order order :type type :value value})))

(defn to-child []
  (fn [import-context record path]
    (let [idx (get-in import-context [:local-context :child-index])
          to-children* (partial to-children :child idx)]
      (-> (update-in import-context [:local-context :child-index] inc)
          (to-children* record path)))))
  
(defn to-parent [offset] (partial to-parents :parent offset))

(defmacro to-event [prefix]
  `(partial nested-handler*
    {
     :DATE (to-group (keyword (str (name ~prefix) "-date")))
     :PLAC (to-group (keyword (str (name ~prefix) "-place")))
     :NOTE (to-group (keyword (str (name ~prefix) "-note")))
     :TEMP (to-group (keyword (str (name ~prefix) "-temple")))
     :STAT (to-group (keyword (str (name ~prefix) "-child-status")))
     }))


(def fam-handler-map
{

 :HUSB (to-parent 0)
 :WIFE (to-parent 1)
 :CHIL (to-child)

 :MARR (to-event :marriage)
 :DIV  (to-event :divorce)
})
   
   
;; (defn- top-level-handler [handlers ps {:keys [tag value] :as record} path & more]
;;   (let [subhandler (nested-handler handlers skip-handler)
;;         [new-ps state-2 :as rtnval] (apply subhandler ps record path {:fam-state 1 :child-index 0} more) 
;;         ]
;;     ;(println "top-level-handler <<" state-2 new-ps value)
;;     (let [persona-v (add-vertex (merge (:persona state-2) {:type :persona }))
;;           famvidual-v (add-vertex (merge (:persona state-2) {:type :famvidual }))]
;;       (add-edge famvidual-v :member persona-v)
;;       (add-edge (:source new-ps) :contributes persona-v)
;;       (add-edge *researcher* :accepts famvidual-v)
      
;;       (-> new-ps
;;          (assoc-in [:id-map value] famvidual-v)) 
;; )))

(def fam-nested-handler (partial nested-handler* fam-handler-map))

(defn fam-link-member [db  pgroup igroup {label :type :as member} 
                       {:keys [persona individual]}]
  (add-edge db pgroup label persona member)
  (add-edge db igroup label individual member))

(defn fam-post-processor [ db source {:keys [type] :as group-map} parents children id-map]
  ;;(println "fam-post-processor:" parents children id-map)
  (let [persona-group (add-node db (merge group-map {:type :group}))
        individual-group (add-node db (merge group-map {:type :group}))]
    (add-edge db source :contributes persona-group {})
    (add-edge db source :contributes individual-group {})
    (doseq [member (concat parents children)]
      (fam-link-member db persona-group individual-group member (get id-map (:value member))))))
      


(defn fam-handler 
  "Read each record.  The relationship tags (HUSB, WIFE, CHIL) get added to a vector of relationships.  The events get added to the group-map.  Once all the infomation is gathered:
     1. Create the group vertex
     2. Create an edge from source to group label contributes

3. Create edges from the group to the persona vertices using the informaton on the members vector
     4. Create the links from the parent individual to the child with label 'lineage'"
  [{:keys [db user shared-context id-map] :as import-context} {:keys [value] :as record} path]
  (let [local-context (->FamContext {} [] [] 0)
        {:keys [source]} shared-context
        rtnval (fam-nested-handler (assoc import-context :local-context local-context)
                                   record path)
        {:keys [group-map parents children] :as local-context} (:local-context rtnval)]
         
         (fam-post-processor db source group-map parents children id-map)
         rtnval))
         




