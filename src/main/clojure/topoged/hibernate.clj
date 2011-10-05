(ns topoged.hibernate
  (:use [org.satta.glob])
  (:require [topoged.util :as util])
  (:require [topoged.log :as log]))


(defn hibernate-properties-config
  ([prop-file & hbm-globs]
      (let [props (doto (java.util.Properties.)
		    (.load (java.io.FileReader. prop-file))
		    )
	    cfg (doto (org.hibernate.cfg.AnnotationConfiguration.)
		  (.addProperties props)
		  )]
	(doseq [hbm-glob hbm-globs]
	  (doseq [file (glob hbm-glob)]
	    (let [p (.getAbsolutePath file)]
	      ( .addFile cfg p))))
	cfg)))

(defn init [cfg-fn]
  (let [hsf (memoize #(.buildSessionFactory ( cfg-fn)))]
    (letfn [( begin-tx-local []
			     (let [^org.hibernate.impl.SessionFactoryImpl sf (hsf)
				   session (.. sf openSession (getSession org.hibernate.EntityMode/MAP))
				   tx (. session beginTransaction)]
			       [session tx]))]
      (intern 'topoged.hibernate 'begin-tx begin-tx-local))))

(defmacro with-hibernate-tx
  "Execute body in the context of a hibernate trasnascton.  The session and tx parameters are
set with the hibernate session and a transaction.  The transaction is commited unless an Exception is
thrown in body.  If there is an unhandled exception thrown in body, the transaction will be rolled back.  The session is also closed regardless of any exceptions"
  [[session tx] & body]
  (let [src (gensym "src") rtnval (gensym "rtnval") ex (gensym "ex") log (gensym "log")]
    `(let [~src (begin-tx)
	   ~(with-meta session {:tag 'org.hibernate.Session}) (first ~src) 
	   ~(with-meta tx {:tag 'org.hibernate.Transaction}) (second ~src)]
       (try
	 (let [~rtnval  ~@body]
	   (. ~session flush)
	   (. ~tx commit)
	   ~rtnval)
	 (catch java.lang.Exception ~ex
	   (try
	     (if (and ~tx (.isActive ~tx))
	       (.rollback ~tx))
	     (finally
	      (log/with-log [~log]
		(log/log-error ~log ~ex "Rollback failed")
		(throw ~ex)))))
	 (finally
	  (if (and ~session (. ~session isOpen))
	    (.close ~session)))))))
