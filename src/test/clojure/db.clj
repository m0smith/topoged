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
  (let [id (uuid)
	msb (.getMostSignificantBits id)
	lsb (.getLeastSignificantBits id)]
    (byte-array (mapcat long-to-bytes [msb lsb]))))

(def hsf (memoize hibernate-session-factory))

(defn add-type [name desc]
		      (let [^org.hibernate.impl.SessionFactoryImpl sf (hsf)
			    sessionp (. sf openSession)
			    session (. sessionp getSession org.hibernate.EntityMode/MAP)
			    tx (. session beginTransaction)
			    entity "Type"
			    m1 (doto (java.util.HashMap.)
				 (.put "desc" desc)
				 (.put "name" name)
				 (.put "id" (id-factory)))
			    rtnval (. session save entity  m1)]
			      (. session flush)
			      (. tx commit)
			      (. session close)
			      rtnval))

			   
(defn list-types [] (let [^org.hibernate.impl.SessionFactoryImpl sf (hsf)
			   sessionp (. sf openSession)
			   session (. sessionp getSession org.hibernate.EntityMode/MAP)
			   tx (. session beginTransaction)
			   rtnval (.. session (createQuery "from Type") list)]
				(. session flush)
				(. tx commit)
				(. session close)
				rtnval))


