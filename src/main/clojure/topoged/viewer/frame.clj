(ns topoged.viewer.frame
  (:require [topoged.data.inmemory :as db])
  (:use [topoged.gedcom :only (gedcom-seq)]
	[topoged.viewer.status]
	[topoged.service.plugin.info]
	[topoged.plugin.gedcom.import.core :only (gedcom-import-action)]
    [seesaw core graphics]))

  
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

(def lb (listbox :model [[ 1 "Emtpy"]] :renderer render-name-item))

(def pedigree-panel
  (grid-panel :border "Pedigree"))

(defn build-pedigree-panel "return root-panel"
  [id root-panel offset]
  ;(println "build-pedigree-panel:" id)
  (let [child (first (db/entity :id id))
        [father mother] (db/parents-of id)]
    ;(println "build-pedigree-panel:" father mother child)
    (when child
      (config! root-panel :items nil)
      (config! root-panel
               :border [2 (str offset) 2] 
               :north (apply str (take 25 (:name child)))
               :west (build-pedigree-panel father (border-panel) (* 2 offset))
               :east (build-pedigree-panel mother (border-panel) (inc (* 2 offset)))))
    root-panel))

(def top-frame (border-panel
                :size [500 :by 500]
                :center (left-right-split
                         (scrollable lb)
                         pedigree-panel)
                :north (label :text "TOPOGED" :h-text-position :center)
                :south status-bar))

(defn a-import-gedcom-handler [e]
  (let [pi (create-plugin-info top-frame top-frame)]
     (gedcom-import-action (assoc pi :status status-bar))
    (config! lb :model (sort-by second (db/persona-names)))))


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
  (let [panel (build-pedigree-panel (first (selection e)) (border-panel) 1)]
    (config! pedigree-panel :items [ panel ])))
           

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

(defn -main []
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

			 


