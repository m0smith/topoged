(ns topoged.log
  (:import [org.slf4j LoggerFactory]))



(def log-factory
     (letfn [(logger [name]
		     (println "creating logger" name)
		     (LoggerFactory/getLogger name))]
       (memoize logger)))

(defmacro with-log [[ logger] & body]
  `(let [~logger (log-factory (str *ns*))] ~@body ))

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
       