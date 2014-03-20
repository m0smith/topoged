(ns basic
  (:use [topoged.init :only [topoged-init]])
  (:require[archimedes.core :as g]
           [archimedes.vertex :as v]
           [archimedes.edge :as e]
           [ogre.core :as q]
           [topoged.db :as tdb]))

;;; (v/create! data-map)
;;; (e/connect-with-id! nil start label end data-map)

;;;
;;; Start Schema Tools
;;;

(defprotocol SchemaEdge
  (edge-direction [_ end-points] [_ start end] 
    "Return a vector with start and end in order for this type of
  edge"))

(defprotocol SchemaPath
  (path-reverse [ele])
  (path-to-q [ele] "Return a function that operates in a query"))

(defrecord NodeDefinition [label reqs]
  SchemaPath
  (path-reverse [ele] ele)
  (path-to-q [ele] identity))

(defrecord EdgeDefinition [start label end reqs]
  SchemaEdge
  (edge-direction [_ end-points] end-points)
  (edge-direction [_ s e] [s e]))


(defrecord ReverseEdgeDefinition [end label start reqs]
  SchemaEdge
  (edge-direction [_ end-points] (rseq end-points))
  (edge-direction [_ s e] [e s])
  SchemaPath
  (path-reverse [{:keys [start label end reqs]}] (->EdgeDefinition start label end reqs))
  (path-to-q [{:keys [start label end reqs]}] 
    (fn [f] (-> f (q/<E-- [label]) q/out-vertex))))

;; Broken out because ReverseEdgeDefinition wasn't defined until now
(extend-protocol  SchemaPath
  EdgeDefinition
  (path-reverse [{:keys [start label end reqs]}] (->ReverseEdgeDefinition end label start reqs))
  (path-to-q [{:keys [start label end reqs]}]
    (fn [f] (-> f (q/--E> [label]) q/in-vertex))))


(defmacro defnode [name label reqs]
  `(def ~name (->NodeDefinition ~label ~reqs)))

(defmacro defedge [name start label end reqs]
  (let [s (symbol (str "-" `~name "->"))
        s2 (symbol (str "<-" `~name "-"))]  
    `(do
       (def ~s (->EdgeDefinition ~start ~label ~end ~reqs))
       (def ~s2 (->ReverseEdgeDefinition ~end ~label ~start ~reqs)))))

(defn add-node! [db type data-map]
  (tdb/add-node db (merge {:label (:label type) } data-map)))

(defn add-edge! 
  ([db start type end]
     (add-edge! db start type end {}))
  ([db start {:keys [label] :as type}  end data-map]
     ;(println "start:" start " label:" label " direction-fn:" direction-fn " end:" end)
     (let [[start end] (edge-direction type start end)]
       (tdb/add-edge db start label end data-map))))

;;
;; Path
;; A path is a vector with a Node or a [Node Edge Node] or with
;; any number of repeting Edge Node appended.  It repesent a single path
;; throught the graph. 

(defn path-rev [path] 
  "Create a path that is the reverse of `path`.  This path will have
the direction of the edges reversed as well"
  (vec (map path-reverse (rseq path))))

(defn path-conj [p1 p2]
  "Append `p2` to the end of `p1`.  The first element of p2 is dropped
as it must match the last element of p1"
  (apply conj p1 (subvec p2 1)))

(defn path-query** [cum arg]
  (let [f (path-to-q arg)]
    (f cum)))

(defn path-query* [f path]
  "Assuming the nodes in `f`, apply `path` to those nodes."
  (reduce path-query** f path)) 

(defn path-query [path node]
  "Look for all paths matching `path` and stating with `node`.
Eliminates paths back to the starting node"
  (q/query
   (v/find-by-id (v/id-of node))
   (path-query* path)
   (q/except [(v/find-by-id (v/id-of node))])
   q/path
   q/all-into-vecs!))


;;;
;;; Define Schema
;;;

(defnode Individual :individual 
  [
   :name ;the print name1
   ])

(defnode Birth :birth 
  [
   ])

(defnode Marriage :marriage 
  [
   ])


(defedge Parent Birth :parent Individual 
  [
   :order ; 0 = father, 1 = mother
   ])

(defedge Child Birth :child Individual 
  [
   :order ; Order of the child in the family
   ])

(defedge Spouse Marriage :spouse Individual 
  [
   :order ; 0 = husband and 1 = wife
   ])

(def child->parent-path [ Individual <-Child- Birth -Parent-> Individual ])

(def marriage-path [ Individual <-Spouse- Marriage -Spouse-> Individual ])

(defn add-parents [db child father mother order]
  (when child
    (when (or father mother)
      (let [b (add-node! db Birth {:order order})]
        (add-edge! db b -Child->  child)
        (when father
          (add-edge! db father <-Parent- b {:order 0}))
        (when mother
          (add-edge! db mother <-Parent- b {:order 1}))))))

(defn parents-of [node]
  (let [m (reduce conj {}
                  (for [[_ _ _ edge parent] (path-query child->parent-path node)]
                    [(v/get edge :order) parent]))]
    [(get m 0) (get m 1)]))


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
    (map parents-of (tdb/find-by-kv db :label :individual))))
;    (map #(vector % (-> % v/all-edges-of seq)) (tdb/find-by-kv db :label :individual))))
