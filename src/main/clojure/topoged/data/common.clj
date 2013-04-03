(ns topoged.data.common
  (:use [topoged.util]
        [slingshot.slingshot :only [throw+]]
        [clojure.core.logic :rename {== ?==}]))


(declare add-to-data-store)
(declare indatastoreo)
(declare by-indexo)
(declare init)
(declare shutdown)
(declare dbsync)

;;(def UNDEFINEDX #uuid "a4d6c4d6-bb29-45ca-8bf6-25c06168a8d5")
(def UNDEFINED nil)

(def persona-keys [ :key :sourceId ] )
(def persona-type :persona-document)

(def source-keys [ :key ] )
(def source-type :source-document)

(def attachment-keys [ :key :sourceId ])
(def attachment-type :attachment-document)

(def attribute-keys [ :key :type :owner ])
(def attribute-type :attribute-document)

(def group-keys [ :key :sourceId :type :members])
(def group-type :event-group-type)

(def lineage-keys [:sourceId :parents :children] )
(def lineage-type :GROUP)
(def lineage-group-type :LINEAGE)

(defn illegal-args [msg val]
  (throw+ {:message msg :args val :class (class val)}))

(defn- args-to-map 
  ([ f ] 
     (cond
      (map? f) f
      (coll? f) (apply args-to-map f)
      :else (illegal-args "A single arg must be map or a collection" f)))
  ([ k v & args ] 
     (if (even? (count args)) 
       (apply hash-map k v args)
       (illegal-args "Either a map or key value pairs" args))))

(defn- has-keys [m kys]
  (every? m kys))

(defn validate-args [ kys & args ]
  (let [m (apply args-to-map args)]
    (if (has-keys m kys)
      m
      (throw+ {:message "Missing required keys: " :keys kys :args args}))))

(defn mmap [f coll]
  (map #(map f %) coll))

(defn ffilter [flt pred coll]
  (filter #(flt pred %) coll))

(defn mapvkeys [kys coll]
  (map #(mapv % kys) coll))

(defn ffilterp [pred coll]
  (ffilter some pred coll))

(defn filter-get [k pred coll]
  (ffilter pred #(get % k) coll))


;;
;; Create entities
;;

(defn init-group [sourceId]
  {:sourceId sourceId
   :parents [ [0 UNDEFINED] [1 UNDEFINED]]
   :children []})




;;          )))))
;; Search for records
;;


(defn entityo
  "Pass logic variables map and result.  Result will be unified with all records
that have at least the values in the map specied"
  [m r]
  (fresh [?pm]
         (featurec ?pm m)
         (indatastoreo ?pm m)
         (?== r ?pm)))
         
(defn entities "Return all the entities matching the args"
  ([& args]
     (when args
       (let [m (apply validate-args [] args)]
         (run* [q]
               (entityo m q))))))

(def entity entities)

(defn entityv "Return all the values of the entities as a vector"
  [kys & args]
  (mapvkeys kys (apply entity args)))

(defn entitypv "Return all the valeus of the entities that match the pred"
  [pred kys & args]
  (filter pred (apply entityv kys args)))

(defn persona-names []
  (entityv [:id :name] :type persona-type))

(defn parento
  "Unify the child id with the parent id with the parent order (0=father, 1=mother)"
  [child parent order] 
  (fresh [?pm ?parents  ?parentrec ?children]
         (?== ?parentrec [order parent])
         (featurec ?pm {:groupType lineage-group-type :parents ?parents :children ?children})
         (indatastoreo ?pm {:groupType lineage-group-type :parents ?parents :children ?children})                
         (membero child ?children)
         (membero ?parentrec ?parents)
         ))

(defn grandparento [child grandparent]
  (fresh [?p ?order ?o2]
         (parento child ?p ?order)
         (parento ?p grandparent ?o2)))

(defn ancestoro [c a] 
  (conde  
   [(fresh [p x ?order] 
           (parento c p ?order) 
           (ancestoro p x)
           (appendo [p] x a))] 
   [(fresh [x ?order] 
           (parento c x ?order) 
           (?== a [x]))]))

(defn parents-of
  "Returns the :id of the parents of child.  If there are no parents return an empty list.
The order is first father, second mother and after any other parents"
  [child]
  (when child
    (map second
         (sort-by first
                  (run* [q]
                        (fresh [?parent ?order]
                               (parento child ?parent ?order)
                               (?== q [?order ?parent])))))))
  
(defn children-of
  "Returns the :id of the children of [arent.
If there are no children return an empty list.
The order is undefined"
  [parent]
  (when parent
    (run* [q]
          (fresh [?child ?order]
                 (parento ?child parent ?order)
                 (?== q ?child)))))
  

(defn anc [child]
  (run* [q]
       (ancestoro child q)))


(defn lookup [k]
  (ffirst (entityv [:id] :key k)))


(defn key-id* [a]
  (cond
   (keyword? a) (if-let [id (lookup a)]
                  id
                  (throw+ {:arg a :message "Key not defined as a type"}))
   (instance? java.util.UUID a) a
   :else (throw+ {:arg a :class (class a) :message "Unsupported type"})))

(def key-id (memoize key-id*))

(defn new-attachment-document []
  {
   :id (uuid)
   :key attachment-type
   :docType (key-id attachment-type)
   })


(defn include-basic-keys [{:keys [id docType key] :as m}]
  (let [rtnval {}]
    (when-not id (assoc m :id (uuid)))
    (when-not docType (assoc  :docType (key-id key)))
    (merge m rtnval)))

;;
;; Add recortds
;;


(defn add-entity [req-keys type & args ]
  (-> (apply validate-args req-keys args) include-basic-keys add-to-data-store))

(defn add-persona [  & args ]
  (apply add-entity persona-keys persona-type args))

(defn add-source [ & args ]
  (apply add-entity source-keys source-type args))

(defn add-lineage-group [ & args ]
  (apply add-entity lineage-keys lineage-type [(assoc (first args) :groupType lineage-group-type)]))
