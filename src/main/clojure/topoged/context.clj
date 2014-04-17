(ns topoged.context
  (:use [clojure.algo.monads]))


(defmacro with-ctx-named [ ctx & body ]
	`(domonad reader-m ~ [ctx (ask)]  ~@body))

(defmacro with-ctx-keys [ ctx & body ]
	`(domonad reader-m ~ [{:keys ctx} (ask)]  ~@body))

(defmacro with-ctx-keys2 [ ctx bs & body ]
	`(domonad reader-m 
                  ~(if (seq bs) 
                     (apply conj [{:keys ctx} (ask)] bs) 
                     [{:keys ctx} (ask)])  
                  (do ~@body)))

(defmacro with-ctx2 [ ctx bs & body ]
	`(domonad reader-m ~(if (seq bs) (apply conj [ctx (ask)] bs) [ctx (ask)])  ~@body))

(defmacro with-ctx [ & body ]
	`(domonad reader-m ~@body))

(defmacro defnctx [name & body]
  `(defn ~name []
     (with-ctx  ~@body)))

(defmacro defnctx-keys [name kys & body]
  `(defn ~name []
     (with-ctx-keys ~kys
       ~@body)))


(defmacro defnctx-callback   [name args bnds & body]
  `(defn ~name []
     (with-ctx ~bnds
       (fn ~args
         ~@body))))

(defmacro defnctx-keys-callback   [name args kys & body]
  `(defn ~name []
     (with-ctx-keys ~kys
       (fn ~args
         ~@body))))

(defn apply-ctx [ factory ctx ]
  ((factory) ctx))
