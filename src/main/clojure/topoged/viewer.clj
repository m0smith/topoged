(ns topoged.viewer
  (:use [topoged.gedcom :only (gedcom-seq)]
	[topoged.file :only (input-stream output-stream copy-md5)]
	[topoged.gedoverse
	 :only (add-persona personas persona-cause sources add-source log)])
  (:import
   (java.io File InputStream OutputStream)
   (javax.swing DefaultListModel JFileChooser JFrame JMenu JMenuBar JMenuItem JLabel JList JPanel JScrollPane ListModel SwingUtilities Timer)
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


(def status-agent (agent (list "Init")))


(defn import-gedcom [file]
  (let [tempfile (File/createTempFile "topoged-" ".ged")
	uuid (str (java.util.UUID/randomUUID))
	md5 (with-open [^InputStream r (input-stream file) ^OutputStream w (output-stream tempfile)]
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

(defn gedcom-import-action [^JFileChooser fc frame update-list-model list-model]
  (. fc showOpenDialog frame)
  (if-let [ file (.getSelectedFile fc)]
    (future
     ((persona-cause
       #(send status-agent (fn [l] (conj l (str "Importing " file))))))
     (let [status (import-gedcom file)]
       ((persona-cause #(update-list-model list-model)))
       ((persona-cause
	 #(send status-agent (fn [l] (conj l (:message status))))))))))

(defn viewer-app []
  ""
  (let [timer (Timer. 1000 nil)
	frame  (JFrame. "Topoged Viewer")
	^JLabel status-info (JLabel.)
	close-window (fn [] (.stop timer) (.dispose frame))
	update-list-model (fn [^DefaultListModel m]
			    (. m clear)
			    (reduce #(doto ^DefaultListModel %1 (.addElement %2))
				    m
				    (map first (display-personas))))
	^ListModel list-model (update-list-model ( DefaultListModel.))
	^JFileChooser fc (JFileChooser. ".")]
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
			   (gedcom-import-action fc frame update-list-model list-model))))
			    
		 (.add (doto (JMenuItem. "Exit")
			 (with-action _ (close-window))))))))
		
      (.setSize 200 200)
      (.pack)
      ;;(.setDefaultCloseOperation (JFrame/EXIT_ON_CLOSE))
      (.setVisible true))))
     


;;(SwingUtilities/invokeLater flipperviewer-app)


			 


