(ns topoged.data.schema
  (:import [com.tinkerpop.blueprints.impls.tg TinkerEdge TinkerVertex])
  (:require [ogre.core :as q]
            [archimedes.vertex :as v]
            [topoged.db :as tdb]))

;;;
;;; Start Schema Tools
;;;

(defprotocol SchemaEdge
  (edge-direction [_ end-points] [_ start end] 
    "Return a vector with start and end in order for this type of
  edge"))

(defprotocol SchemaPath
  (path-add-node [def val])
  (path-add-edge [def val n1 n2])
  (path-reverse [ele])
  (path-to-q [ele] "Return a function that operates in a query"))


(defprotocol NodeManager
  (node-create [node db type])
  (to-data-map [node])
  (merge-node [node data-map]))

(defprotocol EdgeManager
  (edge-create [edge db type node1 node2]))



(defrecord NodeDefinition [label reqs]
  SchemaPath
  (path-reverse [ele] ele)
  (path-to-q [ele] identity))

(defrecord EdgeDefinition [start label end reqs]
  SchemaEdge
  (edge-direction [_ end-points] end-points)
  (edge-direction [_ s e] [s e]))


(defn prop-filter [node ky v]
  (= (v/get v ky) (get node ky)))

          

(defrecord ReverseEdgeDefinition [end label start reqs]
  SchemaEdge
  (edge-direction [_ end-points] (rseq end-points))
  (edge-direction [_ s e] [e s])
  SchemaPath
  (path-reverse [{:keys [start label end reqs]}] (->EdgeDefinition start label end reqs))
  (path-to-q [{:keys [start label end reqs]}] 
    (fn [f] (-> f (q/<E-- [label]) 
                (q/as (str(gensym))) 
                q/out-vertex 
                (q/as (str(gensym)))
                (q/filter (partial prop-filter start :label))
                ))))

;; Broken out because ReverseEdgeDefinition wasn't defined until now
(extend-protocol  SchemaPath
  EdgeDefinition
  (path-reverse [{:keys [start label end reqs]}] (->ReverseEdgeDefinition end label start reqs))
  (path-to-q [{:keys [start label end reqs]}]
    (fn [f] (-> f (q/--E> [label]) 
                (q/as (str(gensym))) 
                q/in-vertex 
                (q/as (str(gensym)))
                (q/filter (partial prop-filter end :label))

                ))))


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

(extend-protocol NodeManager
  TinkerVertex
  (node-create [node _ _] node)
  (to-data-map [m] (v/to-map m))
  (merge-node [node data-map] (v/merge! node data-map))
  java.util.Map
  (node-create [node db type] (add-node! db type node))
  (to-data-map [m] m)
  (merge-node [node data-map] (merge node data-map)))

(extend-protocol EdgeManager
  java.util.Map
  (edge-create [edge db type node1 node2]
    (add-edge! db node1 type node2 edge))
  TinkerEdge
  (edge-create [edge _ _ _ _] edge))



;;
;; Path
;; A path is a vector with a [Node] or a [Node Edge Node] or with
;; any number of repeting Edge Node appended.  It repesent a single path
;; through the graph. 

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

(defn path-query 
  "Look for all paths matching `path` and stating with `node`.
Eliminates paths back to the starting node"
  ([path]
     (let [type (first path)]
       (q/query
        (v/find-by-kv :label (get type :label))
        (q/as "start")
        (path-query* path)
        q/select
        q/all-into-vecs!)))
  ([path node]
     (q/query
      (v/find-by-id (v/id-of node))
      (q/as "start")
      (path-query* path)
      (q/except [(v/find-by-id (v/id-of node))])
      q/select
      q/all-into-vecs!)))



(defn path-create** [db [t1 n1] [edge-type edge] [t2 n2]]
  (let [node1 (node-create n1 db t1)
        node2 (node-create n2 db t2)
        e (edge-create  edge db edge-type node1 node2)]
    [[t1 node1] [edge-type e][t2 node2]]))

(defn path-create* [db rtnval [[et nt2] [e n2] :as args] ]
  (let [node1 (last rtnval)]
    (apply conj rtnval (subvec (path-create** db node1 [et e] [nt2 n2]) 1))))

(defn path-create [db pathdef node-defs]
  ""
  (println "NODE-DEFS:" node-defs)
  (let [n (node-create  (first node-defs) db (first pathdef))
        nt (map vector (partition 2 (drop 1 pathdef)) (partition 2 (drop 1 node-defs)))]
    (map second (reduce (partial path-create* db) [[(first pathdef) n]] nt))))

