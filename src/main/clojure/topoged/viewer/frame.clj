(ns topoged.viewer.frame

   (:import [java.io FileNotFoundException])
   (:require [topoged.data.common :as db]
             [topoged.data.inmemory])
   (:use [topoged.gedcom :only (gedcom-seq)]
         [topoged.viewer status common]
         [topoged.service.plugin.info]
         [topoged.viewer.pedigree tree fractal]
         [topoged.plugin.gedcom.import.core :only (gedcom-import-action)]
         [seesaw core graphics tree]))
  
;; (defn display-personas []
;;   (let [p @persona-agent]
;;     (sort-by first (map (fn [x] [(:name (val x)) (key x)]) (seq p)))))

;; (defmacro with-action [component event & body]
;;   `(. ~component addActionListener
;;       (proxy [java.awt.event.ActionListener] []
;;         (actionPerformed [~event] ~@body))))

;; (defmacro with-window-closing [component event & body]
;;   `(. ~component addWindowListener
;;       (proxy [java.awt.event.WindowAdapter] []
;; 	(windowClosing  [~event] ~@body))))

;; (defn add-sub-panel [panel]
;;   (let [sub-panel (JPanel.)]
;;     (.add panel sub-panel)
;;     sub-panel))


;;TextArea text = new TextArea();
;;        Font font = new Font("Serif", Font.ITALIC, 20);
;;        text.setFont(font);
;;        text.setForeground(Color.blue);
;;        f.add(text, BorderLayout.CENTER);

;; (defn viewer-app []
;;   ""
;;   (let [frame  (JFrame. "Topoged Viewer .1")
;;         ^JPanel status-info (doto (JPanel.) (.add (JLabel. "Status:")))
;;         ^JPanel view-panel  (doto ( JLabel. "Topoged .1")
;;                               (.setFont (Font. "Serif" Font/ITALIC, 72))
;;                               (.setForeground Color/BLUE))
;; 	close-window (fn []  (.dispose frame))
;; 	plugin-info (create-plugin-info frame view-panel)]

;;     (with-window-closing frame _ (close-window))
	
;;     (doto frame
;;       (.setContentPane
;;        (doto (JPanel.)
;;          (.setSize 400 600)
;;          (.setLayout (BorderLayout.))
;;          (.add view-panel BorderLayout/CENTER)
;;          (.add (doto (JPanel.)
;;                  (.add status-info))
;;                BorderLayout/SOUTH)))
;;       (.setJMenuBar
;;        (doto (JMenuBar.)
;;          (.add (doto (JMenu. "File")
;;                  (.add (doto (JMenuItem. "Import")
;;                          (with-action _
;;                            (let [f (gedcom-import-action (assoc plugin-info :status (add-sub-panel status-info)))]
;;                              (println @f)))))
                 
;;                  (.add (doto (JMenuItem. "Exit")
;;                          (with-action _ (close-window))))))))
      
;;       (.setBounds 300 300 600 800)
;;       (.setSize 400 600)
;;       (.pack)
;;       (.setDefaultCloseOperation (JFrame/EXIT_ON_CLOSE))
;;       (.setVisible true))))




(def status-bar (label :text "STATUS" :h-text-position :center))


(defn render-name-item
  [renderer {:keys [value]}]
  (config! renderer :text (second value)))

(def lb (listbox 
                 :renderer render-name-item))



(def pedigree-panel-tabs
  (tabbed-panel :placement :top
                :tabs [ {:title "Tree"
                         :content pedigree-panel}
                        {:title "Fractgal"
                         :content pedigree-panel-fractal}]))

(def pedigree-panel-container
  (grid-panel :border "Pedigree" :items [pedigree-panel-tabs]))



(def descendent-panel
  (grid-panel :border "Descendants"))



(defn build-descendent-panel-tree "return root-panel"
  [id root-panel offset]
  ;(println "TREE:" root-panel)
  
  (let [widget (tree :id :tree :model (load-model id m-children-of)
                     :renderer render-fn)]
    ;(println "WIDGET:" (class widget))
    (config! root-panel :items nil)
    (config! root-panel :center (scrollable widget))
    (expand-children widget 32)
    ;(println "TREE END:" root-panel)
    )
  
  root-panel)


(def top-frame (border-panel
                :size [500 :by 500]
                :center (left-right-split
                         (scrollable lb)
                         (top-bottom-split
                         pedigree-panel-container descendent-panel))
                :north (label :text "TOPOGED" :h-text-position :center)
                :south status-bar))

(defn a-import-gedcom-handler [e]
  (let [pi (create-plugin-info top-frame top-frame)]
    (gedcom-import-action (assoc pi :status status-bar))
    (config! lb :model (sort-by second (db/persona-names)))
    (db/dbsync)))


(defn a-settings-handler [e]
  (println e))


(def file-menu
  (menu :text "File"
        :items
        [
         (action :handler #(future (a-import-gedcom-handler %))
                 :name "Import GEDCOM"
                 :tip "Import a GEDCOM file.")
         (action :handler a-settings-handler
                 :name "Settings"
                 :tip "Setting for TOPOGED.")
         ]))

(defn handle-person-selection [e]
  ;(println "SELECTED:" (.getValueIsAdjusting e) e)
  (if (.getValueIsAdjusting e)
    
    (let [id (first (selection e))
          p-panel (build-pedigree-panel-tree id
                                             (border-panel) 1)
          f-panel (build-pedigree-panel-fractal id)
          d-panel (build-descendent-panel-tree id
                                               (border-panel) 1)]
      
      
      (config! descendent-panel :items [ d-panel ])
      (config! pedigree-panel :items [ p-panel ])
      (config! pedigree-panel-fractal :items [ @f-panel ])
      )))
           

(defn viewer-app []
;  (build-pedigree-panel "CHILD" pedigree-panel)
  (listen lb :selection #(future (handle-person-selection %)))
  (-> (frame :title "Topoged",
             :size [500 :by 500]
             :content top-frame
             :menubar (menubar
                       :items [file-menu
                               (menu :text "Reports" :items [])
                               (menu :text "Tasks" :items [])])
             :on-close :exit)
      ;;pack!
      show!))

(defn -main [ & args ]
  ;(println (seq (.getURLs (java.lang.ClassLoader/getSystemClassLoader))))
  (native!)


  (db/init)
  (config! lb :model (sort-by second (db/persona-names)))
  (invoke-later (viewer-app)))

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

			 


