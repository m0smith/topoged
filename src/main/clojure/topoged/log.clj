(ns topoged.log
  (:import [org.slf4j LoggerFactory]))


(def log-factory
     (letfn [(logger [name]
		     (println "creating logger" name)
		     (LoggerFactory/getLogger name))]
       (memoize logger)))

(defmacro with-log [[ logger] & body]
  `(let [~logger (log-factory (str *ns*))] ~@body ))