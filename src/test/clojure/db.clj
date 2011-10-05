(defn long-to-bytes 
  "convert a long into a sequence of 8 bytes. The zeroes are padded to the 
beginning to make the BigInteger constructor happy"
  [^long lng]
  (let [pad (repeat 8 (byte 0))
	bytes (map byte (.. (BigInteger/valueOf lng) toByteArray))]
    (concat (drop (count bytes) pad) bytes)))

(defmacro uuid [] `(java.util.UUID/randomUUID))




(defn hibernate-session-factory-xxx []
  (println "Crreating session factory")
  (let [cfg (org.hibernate.cfg.AnnotationConfiguration.) ]
    (doto cfg
      (.setProperty "hibernate.dialect" "org.hibernate.dialect.DerbyDialect")
      (.setProperty "hibernate.default_schema" "TOPOGED")
      (.setProperty "hibernate.connection.driver_class" "org.apache.derby.jdbc.EmbeddedDriver")
      (.setProperty "hibernate.connection.url" "jdbc:derby:/tmp/topogedDB;create=true")
      (.setProperty "hibernate.connection.username" "")
      (.setProperty "hibernate.connection.password" "")
      (.setProperty "hibernate.current_session_context_class" "org.hibernate.context.ThreadLocalSessionContext")
      (.setProperty "hibernate.default_entity_mode" "dynamic-map")
      (.setProperty "hibernate.dialect" "org.hibernate.dialect.DerbyDialect")
      (.setProperty "hibernate.hbm2ddl.auto" "update")
      (.setProperty "hibernate.transaction.factory_class" "org.hibernate.transaction.JDBCTransactionFactory")
      (.setProperty "hibernate.show_sql" "true")
      (.setProperty "hibernate.generate_statistics" "true")
      (.setProperty "hibernate.use_sql_comments" "false")
      (.addFile "src/test/resources/Type.hbm.xml")
      (.addFile "src/test/resources/TypeGroup.hbm.xml")
      )
    (.. cfg getProperties (store (java.io.FileWriter. "src/test/resources/hibernate.properties") "Hibernate Config"))
    (.buildSessionFactory cfg)))


(defn hibernate-session-factory []
  (let [props (doto (java.util.Properties.)
		(.load (java.io.FileReader. "src/test/resources/hibernate.properties"))
		)
	cfg (doto (org.hibernate.cfg.AnnotationConfiguration.)
	      (.addProperties props)
	      (.addFile "src/test/resources/Type.hbm.xml")
	      (.addFile "src/test/resources/TypeGroup.hbm.xml")
	      )]
    (.buildSessionFactory cfg)))

(defn hibernate-properties-config []
  (let [props (doto (java.util.Properties.)
		(.load (java.io.FileReader. "src/test/resources/hibernate.properties"))
		)
	cfg (doto (org.hibernate.cfg.AnnotationConfiguration.)
	      (.addProperties props)
	      (.addFile "src/test/resources/Type.hbm.xml")
	      (.addFile "src/test/resources/TypeGroup.hbm.xml")
	      )]
    cfg))


(defn init [cfg-fn]
  (let [hsf (memoize #(.buildSessionFactory cfg-fn))]
    (letfn[(begin-tx []
		     (let [^org.hibernate.impl.SessionFactoryImpl sf (hsf)
			   session (.. sf openSession (getSession org.hibernate.EntityMode/MAP))
			   tx (. session beginTransaction)]
		       [session tx]))]
      (defmacro with-hibernate-tx
	"Execute body in the context of a hibernate trasnascton.  The session and tx parameters are
set with the hibernate session and a transaction.  The transaction is commited unless an Exception is
thrown in body.  If there is an unhandled exception thrown in body, the transaction will be rolled back.  The session is also closed regardless of any exceptions"
	[[session tx] & body]
	(let [src (gensym "src") rtnval (gensym "rtnval") ex (gensym "ex")]
	  `(let [~src (begin-tx)
		 ~(with-meta session {:tag 'org.hibernate.Session}) (first ~src) 
		 ~tx (second ~src)]
	     (try
	       (let [~rtnval  ~@body]
		 (. ~session flush)
		 (. ~tx commit)
		 ~rtnval)
	       (catch java.lang.Exception ~ex
		 (try
		   (if (and ~tx (.isActive ~tx))
		     (.rollback ~tx))
		   (finally (throw ~ex))))
	       (finally
		(if (and ~session (. ~session isOpen))
		  (.close ~session))))))))))

(defn id-factory "Creates a 16 element byte array representation of a uuid"
  []
  (let [id (uuid)
	msb (.getMostSignificantBits id)
	lsb (.getLeastSignificantBits id)]
    (byte-array (mapcat long-to-bytes [msb lsb]))))

(def hsf (memoize hibernate-session-factory))

(defn begin-tx []
  (let [^org.hibernate.impl.SessionFactoryImpl sf (hsf)
	session (.. sf openSession (getSession org.hibernate.EntityMode/MAP))
	tx (. session beginTransaction)]
	[session tx]))

(defmacro with-hibernate-tx-old
  "Execute body in the context of a hibernate trasnascton.  The session and tx parameters are
set with the hibernate session and a transaction.  The transaction is commited unless an Exception is
thrown in body.  If there is an unhandled exception thrown in body, the transaction will be rolled back.  The session is also closed regardless of any exceptions"
  [[session tx] & body]
  (let [src (gensym "src") rtnval (gensym "rtnval") ex (gensym "ex")]
    `(let [~src (begin-tx)
	   ~(with-meta session {:tag 'org.hibernate.Session}) (first ~src) 
	   ~tx (second ~src)]
       (try
	 (let [~rtnval  ~@body]
	   (. ~session flush)
	   (. ~tx commit)
	   ~rtnval)
	 (catch java.lang.Exception ~ex
	   (try
	     (if (and ~tx (.isActive ~tx))
	       (.rollback ~tx))
	     (finally (throw ~ex))))
	 (finally
	  (if (and ~session (. ~session isOpen))
	    (.close ~session)))))))



(defmacro add-entity-factory-auto-id
  "A macro that creates a function that will add a record of the given entity"
  [name factory & columns]
  (let [id (gensym "id")]
    `(fn [~@columns] 
       (let [~id (~factory)
	     entity-name# ~name
	     data# ~(into {"id" id} (map (fn [f] {(str f) f}) columns))]
	 (with-hibernate-tx [session# tx#]
	   (.save session# entity-name# (java.util.HashMap. data#)))))))

(defmacro add-entity-factory
  "A macro that creates a function that will add a record of the given entity"
  [name & columns] 
  `(fn [~@columns] 
     (let [entity-name# ~name
	   data# ~(into {} (map (fn [f] {(str f) f}) columns))]
       (with-hibernate-tx [session# tx#]
	 (.save session# entity-name# (java.util.HashMap. data#))))))

(def add-type (add-entity-factory-auto-id "Type" id-factory name desc))

(def add-type-group (add-entity-factory-auto-id "TypeGroup" id-factory group_type rel_type  type_group_members))

(defn fetch-by-id [table id]
  (with-hibernate-tx [session tx]
    (.load session table id)))

(defn list-from [table]
  (with-hibernate-tx [session _]
    (.. session (createQuery (str "from " table)) list)))

(defn list-types [] (list-from "Type"))

(def types '({"id" (39 -34 -67 44 73 102 78 -81 -78 46 -101 70 84 -74 -5 106),
	     "desc" "Relationship type for a simple grouping", "name" "SIMPLE", "$type$" "Type"}
	    {"id" (-9 24 -36 -92 13 4 69 -64 -93 1 102 -55 -23 73 -120 60),
	     "desc" "Mother Role in a family", "name" "MOTHER", "$type$" "Type"}
	    {"id" (-108 120 67 124 -10 10 67 -73 -80 4 -8 -41 -51 31 -17 -102),
	     "desc" "Father Role in a family", "name" "FATHER", "$type$" "Type"}
	    {"id" (23 66 102 74 10 107 78 -53 -109 77 -126 -58 75 -19 111 114),
	     "desc" "Child Role in a family", "name" "CHILD", "$type$" "Type"}
	    {"id" (56 -124 -110 -19 -37 -64 64 17 -102 -112 70 3 -11 -33 -68 26),
	     "desc" "Mother Role in a family", "name" "MOTHER", "$type$" "Type"}
	    {"id" (105 67 14 61 -57 47 66 56 -96 37 95 -121 -91 -85 22 97),
	     "desc" "Family Group", "name" "FAMILY", "$type$" "Type"}))

(defn save-types []
  (with-hibernate-tx [session tx]
    (doseq [type types]
      (let  [ outtype (java.util.HashMap.
		       (update-in type [ "id" ] #( byte-array (map byte %)))) ] 
	( .save session "Type" outtype)))))

(defn dump-types [] (map (fn [kk] (update-in (into {} kk) [ "id" ] #(seq %))) (list-types)))

(defn find-type-id [s] (get (first (filter #(= s (get %"name")) (list-types))) "id"))

;;(add-type-group (find-type-id "FAMILY") (find-type-id "SIMPLE") #{ (fetch-by-id "Type" (find-type-id "FATHER") ) (fetch-by-id "Type" (find-type-id "CHILD") ) })