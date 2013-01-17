(ns topoged.data.common
  (:use [topoged.util]
        [clojure.core.logic :rename {== ?==}]))


(declare add-to-data-store)
(declare indatastoreo)
(declare init)
(declare shutdown)
(declare dbsync)

(def UNDEFINEDX #uuid "a4d6c4d6-bb29-45ca-8bf6-25c06168a8d5")
(def UNDEFINED nil)

(def persona-keys [:sourceId ] )
(def persona-type :PERSONA)

(def source-keys [] )
(def source-type :SOURCE)
(def source-type-gedcom #uuid "feb55486-ac9e-4ec1-a03e-0fece2c29eb8") 


(def lineage-keys [:sourceId :parents :children] )
(def lineage-type :GROUP)
(def lineage-group-type :LINEAGE)

(defn illegal-args [msg]
  (throw (IllegalArgumentException. msg)))

(defn- args-to-map [ [f & rest :as args] ]
  (cond
   (map? f) f
   (even? (count args)) (apply hash-map args)
   :else (illegal-args "Either a map or key value pairs")))

(defn- has-keys [m kys]
  (every? m kys))

(defn validate-args [ kys args ]
  (let [m (args-to-map args)]
    (if (has-keys m kys)
      m
      (illegal-args (str "Missing required keys: " kys)))))


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


;;
;; Add recortds
;;

(defn add-entity [req-keys type args ]
  (let [m (validate-args req-keys args)
        m (assoc m :type type :id (uuid))]
    (add-to-data-store m)
    m))

(defn add-persona [  & args ]
  (add-entity persona-keys persona-type args))

(defn add-source [ & args ]
  (add-entity source-keys source-type args))

(defn add-lineage-group [ & args ]
  (add-entity lineage-keys lineage-type [(assoc (first args) :groupType lineage-group-type)]))


;;          )))))
;; Search for records
;;


(defn entityo
  "Pass logic variables map and result.  Result will be unified with all records
that have at least the values in the map specied"
  [m r]
  (fresh [?pm]
         (indatastoreo ?pm)
         (featurec ?pm m)
         (?== r ?pm)))
         
(defn entity "Return all the entities matching the args"
  ([& args]
     (when args
       (let [m (validate-args [] args)]
         (run* [q]
               (entityo m q))))))

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
         (indatastoreo ?pm)                
         (featurec ?pm {:groupType lineage-group-type :parents ?parents :children ?children})
         (membero child ?children)
         (membero ?parentrec ?parents)
         (?== ?parentrec [order parent])))

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
  

(defn anc [child]
  (run* [q]
       (ancestoro child q)))




