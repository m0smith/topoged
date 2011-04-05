(ns topoged.viewer
  (:use [topoged.gedcom :only (gedcom-seq)]
	[topoged.file :only (input-stream output-stream)]
	[clojure.contrib.duck-streams :only (reader writer copy *buffer-size*)]
	[topoged.gedoverse
	 :only (add-persona personas persona-cause sources add-source log)])
  (:import
   (java.io InputStream OutputStream)
   (javax.swing DefaultListModel JFrame JMenu JMenuBar JMenuItem JLabel JList JPanel JScrollPane ListModel SwingUtilities Timer)
   (java.awt BorderLayout)))

(set! *warn-on-reflection* true)


(def file "src/test/resources/simple.ged")

(defn create-handler-map [uuid]
    { 
     :INDI (fn [record]
	      (let [id (-> record :attrs :value)
		    content (record :content)]
		(add-persona
		 {:id (str uuid "-" id)
		  :source (str uuid)
		  :idInSource id
		  :name (first (map #(-> % :attrs :value) (filter #(= (% :tag) :NAME) content)))})))})

(defn handle-record [handlers record]
  (if-let [handler (handlers (:tag record))]
    (handler record)))

(defn copy-md5  [#^InputStream input #^OutputStream output]
  (let [buffer (make-array Byte/TYPE *buffer-size*)
	digest (java.security.MessageDigest/getInstance "MD5")]
    (loop []
      (let [size (.read input buffer)]
        (if (pos? size)
          (do (.write output buffer 0 size)
	      (.update digest buffer 0 size)
              (recur))
	  (.toString (java.math.BigInteger. 1 (.digest digest)) 16))))))

(def status-agent (agent (list "Init")))


(defn import-gedcom [file]
  (let [tempfile (java.io.File/createTempFile "topoged-" ".ged")
	uuid (str (java.util.UUID/randomUUID))
	md5 (with-open [r (input-stream file) w (output-stream tempfile)]
	      (copy-md5 r w))]
    (if (not-empty (filter #(= md5 (:md5 %)) (vals (sources))))
      (do
	(log (str "Already imported" file))
	{:status :aborted :message (str "Already imported " file)}) 
      (do
	(log (str "MD5 not found" md5))
	((add-source {:id uuid
		      :source file
		      :md5 md5}))
	(let [handlers (create-handler-map uuid)
	      causes (filter
		      identity
		      (map #(handle-record handlers %) (gedcom-seq tempfile)))]
	  (doall (map #(%) causes)))
	{:message (str "Imported " file) }))))
  
(defn display-personas []
  (let [p (personas)]
    (sort-by first (map (fn [x] [(:name (val x)) (key x)]) (seq p)))))

(defmacro with-action [component event & body]
  `(. ~component addActionListener
      (proxy [java.awt.event.ActionListener] []
        (actionPerformed [~event] ~@body))))

(defmacro with-window-closing [component event & body]
  `(. ~component addWindowListener
      (proxy [java.awt.event.WindowAdapter] []
	(windowClosing  [~event] ~@body))))


(defn viewer-app []
  ""
  (let [ag (agent {})

	timer (Timer. 1000 nil)
	frame  (JFrame. "Topoged Viewer")
	^JLabel status-info (JLabel.)
	close-window (fn [] (.stop timer) (.dispose frame))
	update-list-model (fn [^DefaultListModel m]
			    (. m clear)
			    (reduce #(doto ^DefaultListModel %1 (.addElement %2))
				    m
				    (map first (display-personas))))
	^ListModel list-model (update-list-model ( DefaultListModel.))
	^javax.swing.JFileChooser fc (javax.swing.JFileChooser. ".")]
    (with-action timer _
      (do
	
	(prn (. status-info getText) )
	(doto status-info
	  (.setText (first @status-agent)))))
    (with-window-closing frame _ (close-window))
	
    (.start timer)
    (doto frame
      (.setContentPane
       (doto (JPanel.)
	 (.setLayout (BorderLayout.))
	 (.add (JScrollPane. (JList. list-model)) BorderLayout/CENTER)
	 (.add (doto (JPanel.)
		 (.add (JLabel. "Status:"))
		 (.add status-info))
	       BorderLayout/SOUTH)))
      (.setJMenuBar
       (doto (JMenuBar.)
	 (.add (doto (JMenu. "File")
		 (.add (doto (JMenuItem. "Import")
			 (with-action _
			   (. fc showOpenDialog frame)
			   (if-let [ file (.getSelectedFile fc)]
			     (do
			       (send ag
				     (fn [f]
				       (do
					 ((persona-cause #(send status-agent
								(fn [l] (conj l (str "Importing " file))))))
					 (let [msg (import-gedcom file)]
					   ((persona-cause #(update-list-model
							     list-model)))
					   ((persona-cause #(send status-agent
								(fn [l] (conj l (:message msg)))))))
					 f))))))))
			    
		 (.add (doto (JMenuItem. "Exit")
			 (with-action _ (close-window))))))))
		
      (.setSize 200 200)
      (.pack)
      ;;(.setDefaultCloseOperation (JFrame/EXIT_ON_CLOSE))
      (.setVisible true))))
     


;;(SwingUtilities/invokeLater flipperviewer-app)


			 


