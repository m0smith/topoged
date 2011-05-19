(ns topoged.viewer.frame
  (:use [topoged.gedcom :only (gedcom-seq)]
	[topoged.viewer.status]
	[topoged.service.plugin.info]
	[topoged.plugin.gedcom.import.core :only (gedcom-import-action)]
	[topoged.file :only (input-stream output-stream copy-md5)]
	[topoged.gedoverse
	 :only ( log persona-agent)])
  (:import
   (java.io File InputStream OutputStream)
   (javax.swing DefaultListModel JFileChooser JFrame JMenu JMenuBar JMenuItem JLabel JList JPanel JScrollPane ListModel SwingUtilities Timer)
   (java.awt BorderLayout)))

(set! *warn-on-reflection* true)

  
(defn display-personas []
  (let [p @persona-agent]
    (sort-by first (map (fn [x] [(:name (val x)) (key x)]) (seq p)))))

(defmacro with-action [component event & body]
  `(. ~component addActionListener
      (proxy [java.awt.event.ActionListener] []
        (actionPerformed [~event] ~@body))))

(defmacro with-window-closing [component event & body]
  `(. ~component addWindowListener
      (proxy [java.awt.event.WindowAdapter] []
	(windowClosing  [~event] ~@body))))

(defn add-sub-panel [panel]
  (let [sub-panel (JPanel.)]
    (.add panel sub-panel)
    sub-panel))


(defn viewer-app []
  ""
  (let [timer (Timer. 1000 nil)
	frame  (JFrame. "Topoged Viewer")
	^JPanel status-info (doto (JPanel.) (.add (JLabel. "Status:")))
	close-window (fn [] (.stop timer) (.dispose frame))
	update-list-model (fn [^DefaultListModel m]
			    (. m clear)
			    (reduce #(doto ^DefaultListModel %1 (.addElement %2))
				    m
				    (map first (display-personas))))
	^ListModel list-model (update-list-model ( DefaultListModel.))
	plugin-info (create-plugin-info frame)]

    (with-action timer _
      (let [active (concat (-> @status-agent :active vals) (-> @status-agent :completed))]
	(dorun
	 (map 
	  (fn [{:keys [funcs data] :or {funcs [identity]}}]
	    (let  [funcs2 (apply juxt funcs)]
	      (funcs2 data)))
	  active))))

    (with-window-closing frame _ (close-window))
	
    (.start timer)
    (doto frame
      (.setContentPane
       (doto (JPanel.)
	 (.setLayout (BorderLayout.))
	 (.add (JScrollPane. (JList. list-model)) BorderLayout/CENTER)
	 (.add (doto (JPanel.)
		 (.add status-info))
	       BorderLayout/SOUTH)))
      (.setJMenuBar
       (doto (JMenuBar.)
	 (.add (doto (JMenu. "File")
		 (.add (doto (JMenuItem. "Import")
			 (with-action _
			   (let [f (gedcom-import-action (assoc plugin-info :status (add-sub-panel status-info)))]
			     (println @f)))))
			    
		 (.add (doto (JMenuItem. "Exit")
			 (with-action _ (close-window))))))))
		
      (.setSize 200 200)
      (.pack)
      ;;(.setDefaultCloseOperation (JFrame/EXIT_ON_CLOSE))
      (.setVisible true))))
     


;;(javax.swing.SwingUtilities/invokeLater topoged.viewer.frame/viewer-app)


			 


