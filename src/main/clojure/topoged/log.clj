(ns topoged.log
  (:import [org.slf4j LoggerFactory]))


(defprotocol LoggerNameFactory
  (logger-name [src] "" ))

(extend-type clojure.lang.Namespace
  LoggerNameFactory
  (logger-name [namespace] (str namespace)))

(extend-type String
  LoggerNameFactory
  (logger-name [namespace]  namespace))
    

(def log-factory
     (letfn [(logger [name]
		     (println "creating logger" name)
		     (LoggerFactory/getLogger (logger-name name)))]
       (memoize logger)))

(defmacro with-log [[logger] & body]
  `(let [~logger (log-factory *ns*)] ~@body ))

(defmacro with-named-log [[ name logger] & body]
  `(let [~logger (log-factory ~name)] ~@body ))


(defn log-error
  "Designed to be used within with-log.  The log argument is the logger.  If the first objs is a Throwable
it will by passed to the underlying logger.  All the objs will be stringified"
  ([log & objs]
     (if objs
       (let [[first & rest] objs]
	 (if (instance? java.lang.Exception first)
	   (.error log (apply str (interpose " " rest)) first)
	   (.error log (apply str (interpose " " objs)))))
       (.error log ""))))
       