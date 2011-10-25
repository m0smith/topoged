(ns topoged.test.lazy-tree)

(defn children-of [x]
  [ (+ x x x) (+ x x x 1) (+ x x x 2)])

(defn build-node
  ([x] (build-node x 4))
  ([x levels]
      (if x
	{:node x 
	 :children (let [children (children-of x)]
		     (if (> levels 1)
		       (map #(build-node % (dec levels)) children)
		       (lazy-seq (map #(build-node % 4) children))))})))

(defn left [node] (first (:children node)))

(defn right [node] (last (:children node)))

(defn middle [node] (nth (:children node) 1))

(defn find-value [node & funcs]
  (if (seq funcs)
    (recur ((first funcs) node) (rest funcs))
    (:node node)))

(defn find-children [node & funcs]
  (if (seq funcs)
    (recur ((first funcs) node) (rest funcs))
    (map :node (:children  node))))
