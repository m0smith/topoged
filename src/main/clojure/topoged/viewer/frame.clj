(ns topoged.viewer.frame
  (:import [java.io FileNotFoundException])
  (:require [topoged.model.individual :as indi]
            [taoensso.tower :as tower :refer (with-locale with-tscope t *locale*)])
  (:use [topoged init]
        [topoged.viewer status common]
        [topoged.service.plugin.info]
        [topoged.viewer.pedigree tree fractal]
        [topoged.service.plugin.ui :only (ui-choose-file ui-status)]
        [topoged.plugin.gedcom.import.import :only (import-gedcom)]
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


(defn render-name-item [renderer {:keys [value]}]
  (config! renderer :text (second value)))

(def lb (listbox :renderer render-name-item))

;; (defn pedigree-panel-tabs [{:keys [locale]}]
;;   (tabbed-panel :placement :top
;;                 :tabs [ {:title (t locale tower-config :pedigree/Tree)
;;                          :content pedigree-panel}
;;                         {:title "Fractal"
;;                          :content pedigree-panel-fractal}]))

;; (defn pedigree-panel-container [{:keys [locale] :as topoged-context}]
;;   (grid-panel :border (t locale tower-config :pedigree/Pedigree)
;;               :items [(pedigree-panel-tabs topoged-context)]))



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


;; (defn top-frame [topoged-context]
;;   (border-panel
;;    :size [500 :by 500]
;;    :center (left-right-split
;;             (scrollable lb)
;;             (top-bottom-split
;;              (pedigree-panel-container topoged-context) descendent-panel))
;;    :north (label :text "TOPOGED" :h-text-position :center)
;;    :south status-bar))



;; (defn gedcom-import-action [topoged-context plugin-info]
;;   (if-let [ file (ui-choose-file plugin-info)]
;;     (import-gedcom topoged-context file)))

;; (defn a-import-gedcom-handler [{:keys [db] :as topoged-context} e]
;;   (let [tf (top-frame topoged-context)
;;         pi (create-plugin-info tf tf)]
;;     (gedcom-import-action topoged-context (assoc pi :status status-bar))
;;     (config! lb :model (sort-by second (indi/individual-names db)))
;;     ))


(defn a-settings-handler [e]
  (println e))


;; (defn file-menu [{:keys [locale] :as topoged-context}]
;;   (menu :text (t locale tower-config :menu/File)
;;         :items
;;         [
;;          (action :handler #(future (a-import-gedcom-handler topoged-context %))
;;                  :name (t locale tower-config :menu/ImportGedcom)
;;                  :tip (t locale tower-config :menu/ImportGedcomTip))
;;          (action :handler a-settings-handler
;;                  :name (t locale tower-config :menu/Settings)
;;                  :tip (t locale tower-config :menu/SettingsTip))
;;          ]))

(defn handle-person-selection [e]
                                        ;(println "SELECTED:" (.getValueIsAdjusting e) (selection e))
  (if (.getValueIsAdjusting e)
    (future
      (let [id (first (selection e))
            p-panel (future (build-pedigree-panel-tree id
                                                       (border-panel) 1))
            f-panel (future (build-pedigree-panel-fractal id))
            d-panel (future (build-descendent-panel-tree id
                                                         (border-panel) 1))]
        
        
        (config! descendent-panel :items [ @d-panel ])
        (config! pedigree-panel :items [ @p-panel ])
        (config! pedigree-panel-fractal :items [ @@f-panel ])
        ))))


;; (defn viewer-app [ {:keys [locale db] :as topoged-context} ]
;; ;  (build-pedigree-panel "CHILD" pedigree-panel)
;;   (listen lb :selection #(future (handle-person-selection %)))
;;   (-> (frame :title "Topoged",
;;              :size [500 :by 500]
;;              :content (top-frame topoged-context)
;;              :menubar (menubar
;;                        :items [(file-menu topoged-context)
;;                                (menu :text (t locale tower-config :menu/Reports) :items [])
;;                                (menu :text (t locale tower-config :menu/Tasks) :items [])])
;;              :on-close :exit)
;;       ;;pack!
;;       show!))




(defn frame-prepare [{:keys [locale db] :as topoged-context}]
  (let [l10n #(t locale tower-config %)
        local-descendent-panel (grid-panel)

        pedigree-panel-tabs (tabbed-panel :placement :top
                                          :tabs [ {:title (l10n :pedigree/Tree)
                                                   :content pedigree-panel}
                                                  {:title (l10n :pedigree/Fractal)
                                                   :content pedigree-panel-fractal}])

        pedigree-panel-container (grid-panel :border (l10n :pedigree/Pedigree)
                                             :items [pedigree-panel-tabs])
        top-frame (border-panel
                   :size [500 :by 500]
                   :center (left-right-split
                            (scrollable lb)
                            (top-bottom-split
                             pedigree-panel-container local-descendent-panel))
                   :north (label :text "TOPOGED" :h-text-position :center)
                   :south status-bar)
        ]
    (letfn
        [
         (apply-localizations []
           (config! local-descendent-panel :border (l10n :pedigree/Descendants)))

         (viewer-app []
           (try
             (listen lb :selection #(future (handle-person-selection %)))
             (-> (frame :title "Topoged",
                        :size [500 :by 500]
                        :content top-frame
                        :menubar (menubar
                                  :items [(file-menu)
                                          (menu :text (l10n :menu/Reports) :items [])
                                          (menu :text (l10n :menu/Tasks)   :items [])])
                        :on-close :exit)
                 ;;pack!
                 show!)
             (catch Exception ex
               (.printStackTrace ex *out*))))
         
         (file-menu []
           (menu :text (l10n :menu/File)
                 :items
                 [
                  (action :handler #(future (menu-import-gedcom-handler %))
                          :name (l10n :menu/ImportGedcom)
                          :tip (l10n :menu/ImportGedcomTip))
                  (action :handler a-settings-handler
                          :name (l10n :menu/Settings)
                          :tip (l10n :menu/SettingsTip))
                  ]))
         
         (gedcom-import-action [plugin-info]
           (println "gedcom-import-action:")
           (if-let [ file (ui-choose-file plugin-info)]
             (import-gedcom topoged-context file)))
         
         
         (menu-import-gedcom-handler [e]
           (try
             (let [tf top-frame
                   pi (create-plugin-info tf tf)]
               (gedcom-import-action (assoc pi :status status-bar))
               (println (indi/individual-names db))
               (config! lb :model (sort-by second (indi/individual-names db)))
               (println lb))
             
             (catch Exception ex
               (.printStackTrace ex *out*))))
         
         (handle-person-selection [e]
           (if (.getValueIsAdjusting e)
             (future
               (let [id (first (selection e))
                     p-panel (future (build-pedigree-panel-tree id (border-panel) 1))
                     f-panel (future (build-pedigree-panel-fractal id))
                     d-panel (future (build-descendent-panel-tree id (border-panel) 1))]
                 (config! local-descendent-panel :items [ @d-panel ])
                 (config! pedigree-panel :items [ @p-panel ])
                 (config! pedigree-panel-fractal :items [ @@f-panel ])))))
         
         ]
      viewer-app)))


(defn -main [ & args ]
  (native!)
  (let [{ :keys [db] :as topoged-context} (topoged-init)]
    (config! lb :model (sort-by second (indi/individual-names db)))
    (let [app (frame-prepare topoged-context)]
      (invoke-later (app)))))


  


