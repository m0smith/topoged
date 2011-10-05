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
    

(defprotocol LoggerFormatter
  (format [src] "Convert src into a String sutible for log files" ))

(extend-type String
  LoggerFormatter
  (format [obj] obj))

(extend-type Object
  LoggerFormatter
  (format [obj] (str obj)))

(extend-type nil
  LoggerFormatter
  (format [obj] "nil"))


(def log-factory
     (letfn [(logger [name]
		     (println "creating logger" name)
		     (LoggerFactory/getLogger (logger-name name)))]
       (memoize logger)))

(defmacro with-log [[logger] & body]
  `(let [~logger (log-factory *ns*)] ~@body ))

(defmacro with-named-log [[name logger] & body]
  `(let [~logger (log-factory ~name)] ~@body ))

(defn ^String space-separated [ args ]
  "Concatenate the members of agrs separated by spaces.  Apply format to each member of args.
   See the LoggerFormatter protocol."
  (apply str (interpose " " (map format args))))

(defmacro log* [logger level objxs]
  (let [alogger (gensym "logger")]
    `(let [objs# ~objxs
	   ~(with-meta alogger {:tag 'org.slf4j.Logger}) ~logger]
       (if (first objs#)
	 (let [[head# & rest#] objs#]
	   (if (instance? java.lang.Throwable head#)
	     (let [rtnval# (space-separated rest#)] (. ~alogger ~level rtnval# head#) rtnval#)
	     (let [rtnval# (space-separated objs#)] (. ~alogger ~level rtnval#) rtnval#)))
	 (do
	   (. ~alogger ~level "")
	   nil )))))

(defn log-error
  "Designed to be used within with-log.  The logger argument is the logger.  If the first objs is a Throwable
it will by passed to the underlying logger.  All the objs will be stringified"
  ([logger & objs]
     (log* logger error objs)))

(defn log-debug
  "Designed to be used within with-log.  The log argument is the logger.  If the first objs is a Throwable
it will by passed to the underlying logger.  All the objs will be stringified"
  ([logger & objs]
     (log* logger debug objs)))
      