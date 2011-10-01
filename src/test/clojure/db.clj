(defn long-to-bytes 
  "convert a long into a sequence of 8 bytes. The zeroes are padded to the 
beginning to make the BigInteger contructor happy"
  [^long lng]
  (let [pad (repeat 8 (byte 0))
	bytes (map byte (.. (BigInteger/valueOf lng) toByteArray))]
    (concat (drop (count bytes) pad) bytes)))

(defmacro uuid [] `(java.util.UUID/randomUUID))

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

(defmacro with-hibernate-tx
	  [[session tx] & body]
	  (let [src (gensym "sym") rtnval (gensym "rtnval")]
	    `(let [~src (begin-tx)
		   ~(with-meta session {:tag 'org.hibernate.Session}) (first ~src) 
		   ~tx (second ~src)]
	       (try
		 (let [~rtnval  ~@body]
		   (. ~session flush)
		   (. ~tx commit)
		   ~rtnval)
		 (finally
		  (. ~session close))))))



(defmacro add-entity-factory-auto-id
  "A macro that creates a function that will add a record of the given entity"
  [name fact & columns]
  (let [id (gensym "id")]
    `(fn [~@columns] 
       (let [~id (~fact) entity-name# ~name data# ~(into {"id" id} (map (fn [f] {(str f) f}) columns))]
	 (with-hibernate-tx [session# tx#]
	   (.save session# entity-name# (java.util.HashMap. data#)))))))

(defmacro add-entity-factory
  "A macro that creates a function that will add a record of the given entity"
  [name & columns] 
  `(fn [~@columns] 
     (let [entity-name# ~name data# ~(into {} (map (fn [f] {(str f) f}) columns))]
       (with-hibernate-tx [session# tx#]
	 (.save session# entity-name# (java.util.HashMap. data#))))))

(def add-type (add-entity-factory-auto-id "Type" id-factory name desc))

(defn list-types []
  (with-hibernate-tx [session tx]
    (.. session (createQuery "from Type") list)))


