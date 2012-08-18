(ns topoged.viewer.frame
  (:use [topoged.gedcom :only (gedcom-seq)]
	[topoged.viewer.status]
	[topoged.service.plugin.info]
	[topoged.plugin.gedcom.import.core :only (gedcom-import-action)]
	[topoged.gedoverse :only ( log persona-agent)])
  (:import
   (java.io File InputStream OutputStream)
   (javax.swing DefaultListModel JFileChooser JFrame JMenu JMenuBar JMenuItem JLabel JList JPanel JScrollPane ListModel SwingUtilities Timer)
   (java.awt BorderLayout TextArea Font Color)))

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


;;TextArea text = new TextArea();
;;        Font font = new Font("Serif", Font.ITALIC, 20);
;;        text.setFont(font);
;;        text.setForeground(Color.blue);
;;        f.add(text, BorderLayout.CENTER);

(defn viewer-app []
  ""
  (let [frame  (JFrame. "Topoged Viewer .1")
        ^JPanel status-info (doto (JPanel.) (.add (JLabel. "Status:")))
        ^JPanel view-panel  (doto ( JLabel. "Topoged .1")
                              (.setFont (Font. "Serif" Font/ITALIC, 72))
                              (.setForeground Color/BLUE))
	close-window (fn []  (.dispose frame))
	plugin-info (create-plugin-info frame view-panel)]

    (with-window-closing frame _ (close-window))
	
    (doto frame
      (.setContentPane
       (doto (JPanel.)
         (.setSize 400 600)
         (.setLayout (BorderLayout.))
         (.add view-panel BorderLayout/CENTER)
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
      
      (.setBounds 300 300 600 800)
      (.setSize 400 600)
      (.pack)
      (.setDefaultCloseOperation (JFrame/EXIT_ON_CLOSE))
      (.setVisible true))))

(defn -main [] ( viewer-app))

;;(javax.swing.SwingUtilities/invokeLater topoged.viewer.frame/viewer-app)



;	update-list-model (fn [^DefaultListModel m]
;			    (. m clear)
;			    (reduce #(doto ^DefaultListModel %1 (.addElement %2))
;				    m
;				    (map first (display-personas))))
;	^ListModel list-model (update-list-model ( DefaultListModel.))

;(JScrollPane. (JList. list-model))

;    (with-action timer _
;      (let [active (concat (-> @status-agent :active vals) (-> @status-agent :completed))];
;	(dorun
;	 (map 
;	  (fn [{:keys [funcs data] :or {funcs [identity]}}]
;	    (let  [funcs2 (apply juxt funcs)]
;	      (funcs2 data)))
;	  active))))

			 


