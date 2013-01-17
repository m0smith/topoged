(ns topoged.data.inmemory
  (:use [clojure.core.logic :rename {== ?==}])
  (:use [topoged.util])
  (:use [topoged.data.common]))

;;
;; To implement a new data namespace, it is only necessary to implement the following the
;; functions that reference "data-store", namely:
;;   add-to-data-store - this function accepts the given map and adds it to the data store.
;;   indatastoreo      - expects the data-store to look like a seq.  This could be done by
;;                       pre-searching the database or if using Datomic see
;;        https://github.com/clojure/core.logic/wiki/Extending-core.logic-%28Datomic-example%29



;; Database Specific stuff

(def data-store (atom []))

(defn indatastoreo [pm]
  (membero pm @data-store))

(defn add-to-data-store [m]
  (swap! data-store conj m))


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




