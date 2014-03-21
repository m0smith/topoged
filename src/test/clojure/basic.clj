(ns basic
  (:use [topoged.init :only [topoged-init]]
        [topoged.data path schema])
  (:import [com.tinkerpop.blueprints.impls.tg TinkerEdge TinkerVertex])
  (:require[archimedes.core :as g]
           [archimedes.vertex :as v]
           [archimedes.edge :as e]
           [topoged.db :as tdb]))


(defn add-parents [db child father mother order]
  (when child
    (let [template [child {:order order} {} {:order 0} father]
          [child e1 birth _ _] (if father
                                 (path-create db child->parent-path template)
                                 template)]
      (when mother
        (path-create db child->parent-path 
                     [child e1 birth {:order 1} mother])))))

(defn parents-of [node]
  (let [m (reduce conj {}
                  (for [[_ _ _ edge parent] (path-query child->parent-path node)]
                    [(v/get edge :order) parent]))]
    [(get m 0) (get m 1)]))

(defn add-spouse [db s1 s2 ]
  (path-create db marriage-path [ s1 {:order 0} {} {:order 1} s2]))


(def p 
"A nested vector of 3 elements.  The first element is the name, the
second is the nested father and the third is a nested mother" 
[ "Matt"
  [ "Frank" 
    [ "Henry" []]] 
  ["Patricia" 
   [ nil []]
   [ "Helen" []]]])



(defn- create-context []
  (g/use-clean-graph!)
  (let [db (reify tdb/DataStore
             (tdb/add-node [_ data-map] (v/create! data-map))
             (tdb/jung [_] g/*graph*)
             
             (tdb/add-edge [_ start label end data-map] 
               (when (and start label end)
                 (e/connect-with-id! nil start label end data-map)))
             (tdb/find-by-kv [_ ky vl] (v/find-by-kv ky vl)))]

                           
    db))

(defn pof [ [name father mother]]
  (when name
    (let [[fname fpar] father
          [mname mpar] mother]
      (println name father mother)
      [name father mother ])))
  

(defn build 
  ([db] nil)
  ([db name father mother]
     (let [child-node (add-node! db Individual {:name name})]
       (add-parents db child-node (apply build db (pof father)) (apply build db (pof mother)) 0)
       (println "child-node:" name child-node)
       child-node)))

(defn c []
  (let [db (create-context)]
    (apply build db (pof p))
    (map parents-of (tdb/find-by-kv db :label :individual))
    db))

;    (map #(vector % (-> % v/all-edges-of seq)) (tdb/find-by-kv db :label :individual))))
