(ns topoged.util )
(set! *warn-on-reflection* true)

(defn apply-symbol
  "Appy the function named 'func-name' to the list 'alist'.
    func-name can be a function, string, symbol or keyword.
    alist is the list of arguments.
    example:
           (apply-symbol :max '(1 2 3))"
  ( [func-name] (apply-symbol *ns* func-name nil))
  ( [func-name alist] (apply-symbol *ns* func-name alist))
  ( [name-space func-name alist]
      (if (fn? func-name)
        (apply func-name alist)
	(if-let [f (ns-resolve name-space (symbol (name func-name)))]
	  (apply f alist)))))

(defn partition-starting-every
  "Partition the sequence starting each partition when the f is true.  Thanks to Chouser http://groups.google.com/group/clojure/msg/9ec39ef07c92787b"
  [f coll]
  (lazy-seq
    (when-let [[x & xs] (seq coll)]
      (let [[a b] (split-with (complement f) xs)]
        (cons (cons x a) (partition-starting-every f b)))))) 

