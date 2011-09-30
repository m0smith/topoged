(defn long-to-bytes 
  "convert a long into a sequence of 8 bytes. The zeroes are padded to the 
beginning to make the BigInteger contructor happy"
  [^long lng]
  (let [pad (repeat 8 (byte 0))
	bytes (map byte (.. (BigInteger/valueOf lng) toByteArray))]
    (concat (drop (count bytes) pad) bytes)))

(defn uuid [] (java.util.UUID/randomUUID))

(defn hibernate-session-factory []
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
      (.addFile "src/test/resources/Type.hbm.xml"))
    (.buildSessionFactory cfg)))


(defn id-factory []
  (let [^java.util.UUID id (uuid)
	msb (.getMostSignificantBits id)
	lsb (.getLeastSignificantBits id)]
    (byte-array (mapcat long-to-bytes [msb lsb]))))

(def hsf (memoize hibernate-session-factory))

(defn begin-tx []
  (let [^org.hibernate.impl.SessionFactoryImpl sf (hsf)
	session (.. sf openSession (getSession org.hibernate.EntityMode/MAP))
	tx (. session beginTransaction)]
	[session tx]))
    
(defmacro with-hibernate-tx
  [bindings & body]
  `(let ~bindings
     (try
       ~@body
       (finally
	(let [^org.hibernate.Session session# (first ~(bindings 0))
	      tx# (second ~(bindings 0))]
	  (. session# flush)
	  (. tx# commit)
	  (. session# close)
	)))))

(defn add-type [name desc]
  (with-hibernate-tx [[^org.hibernate.Session session tx] (begin-tx)]
    (let [entity "Type"
	  m1 (doto (java.util.HashMap.)
	       (.put "desc" desc)
	       (.put "name" name)
	       (.put "id" (id-factory)))
	  rtnval (. session save entity  m1)]
      rtnval)))


(defn list-types []
  (with-hibernate-tx [[session tx] (begin-tx)]
    (.. session (createQuery "from Type") list)))
