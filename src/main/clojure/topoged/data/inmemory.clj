(ns topoged.data.inmemory
  (:use [clojure.core.logic :rename {== ?==}])
  (:use [topoged.util])
  (:use [topoged.data.common]))

(def db (atom []))

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
    (swap! db conj m)
    m))

(defn add-persona [  & args ]
  (add-entity persona-keys persona-type args))

(defn add-source [ & args ]
  (add-entity source-keys source-type args))

(defn add-lineage-group [ & args ]
  (add-entity lineage-keys lineage-type [(assoc (first args) :groupType lineage-group-type)]))


;;
;; Search for records
;;

(defn entityo [m r]
  (fresh [?pm]
         (membero ?pm @db)
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

(defn parento [child parent order] 
  (let [data @db]
    (fresh [?pm ?parents  ?parentrec ?children]
           (membero ?pm data)
           (featurec ?pm {:groupType lineage-group-type :parents ?parents :children ?children})
           (membero child ?children)
           (membero ?parentrec ?parents)
           (?== ?parentrec [order parent]))))

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




