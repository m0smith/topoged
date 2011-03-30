(ns topoged.viewer
  (:use [topoged.gedcom :only (gedcom-seq)]
	[topoged.gedoverse :only (add-persona personas persona-cause sources add-source log)])
  (:import (javax.swing DefaultListModel JFrame JMenu JMenuBar JMenuItem JLabel JList JPanel JScrollPane SwingUtilities)))

(def file "src/test/resources/simple.ged")

(defn create-handler-map []
  (let [guid (str (java.util.UUID/randomUUID))]
    { :HEAD (fn [record]
	      (add-source
		 {:id guid
		  :head record}))
     :INDI (fn [record]
	      (let [id (-> record :attrs :value)
		    content (record :content)]
		(add-persona
		 {:id (str guid "-" id)
		  :source (str guid)
		  :idInSource id
		  :name (first (map #(-> % :attrs :value) (filter #(= (% :tag) :NAME) content)))})))}))

(defn handle-record [handlers record]
  (if-let [handler (handlers (:tag record))]
    (handler record)))

(defn import-gedcom [file]
  (let [handlers (create-handler-map)
	causes (filter identity (map #(handle-record handlers %) (gedcom-seq file)))]
    (doall (map #(%) causes))))

(defn display-personas []
  (let [p (personas)]
    (sort-by first (map (fn [x] [(:name (val x)) (key x)]) (seq p)))))

(defmacro with-action [component event & body]
  `(. ~component addActionListener
      (proxy [java.awt.event.ActionListener] []
        (actionPerformed [~event] ~@body))))


(defn viewer-app []
  ""
  (let [ag (agent {})
	frame  (JFrame. "Topoged Viewer")
	update-list-model (fn [m]
			    (. m clear)
			    (reduce #(doto %1 (.addElement %2))
				    m
				    (map first (display-personas))))
	list-model (update-list-model (DefaultListModel.))
	]
    (doto frame
      (.setContentPane
       (doto (JPanel.)
	 (.add (JScrollPane. (JList. list-model)))))
      (.setJMenuBar
       (doto (JMenuBar.)
	 (.add (doto (JMenu. "File")
		 (.add (doto (JMenuItem. "Import")
			 (with-action _
			   (let [fc (doto (javax.swing.JFileChooser. ".")
				      (.showOpenDialog frame))
				 file (.getSelectedFile fc)]
			     (if file
			       (do
				 (send ag
				       (fn [f]
					 (do (import-gedcom file)
					     ((persona-cause #(update-list-model
							      list-model)))
					    f)))))))))
			    
		 (.add (doto (JMenuItem. "Exit")
			 (with-action _ (.dispose frame))))))))
		
      (.setSize 200 200)
      (.pack)
      ;;(.setDefaultCloseOperation (JFrame/EXIT_ON_CLOSE))
      (.setVisible true))))
     


;;(SwingUtilities/invokeLater flipperviewer-app)


			 


