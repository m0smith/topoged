(ns topoged.util )
(set! *warn-on-reflection* true)
(defn apply-symbol
  "Appy the function named 'func-name' to the list 'alist'.
    func-name can be a function, string, symbol or keyword.
    alist is the list of arguments.
    example:
           (apply-symbol :max '(1 2 3))"
  ( [func-name alist] (apply-symbol *ns* func-name alist))
  ( [name-space func-name alist]
      (if (fn? func-name)
        (apply func-name alist)
        (apply (ns-resolve name-space (symbol (name func-name))) alist))))

