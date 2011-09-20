(defn x3 [x] (* 3 x))

(defn p3 [x] [(dec (x3 x)) (x3 x) (inc (x3 x))])

(defn bft [root parent-func val-func]
  (letfn [(anc-internal [q]
			(lazy-seq
			 (if-let [f (peek q)]
			   (let [p (parent-func f)]
			     (cons (val-func f)
				   (anc-internal (apply conj
							(pop q) p)))))))]
    (let [q (clojure.lang.PersistentQueue/EMPTY)]
      (anc-internal (conj q root)))))



;;Shokushu