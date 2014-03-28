(ns topoged.viewer.frame
  (:import [java.io FileNotFoundException])
  (:require [topoged.model.individual :as indi])
  (:use [topoged init][topoged.data schema path]
        [topoged.viewer status common]
        [topoged.service.plugin.info]
        [topoged.viewer.pedigree tree fractal]
        [topoged.service.plugin.ui :only (ui-choose-file ui-status)]
        [topoged.plugin.gedcom.import.import :only (import-gedcom)]
        [seesaw core graphics tree]))

(defn a-settings-handler [e]
  (println e))


(defn gedcom-import-action [topoged-context plugin-info]
  (println "gedcom-import-action:")
  (if-let [ file (ui-choose-file plugin-info)]
    (import-gedcom topoged-context file)))


(defn render-name-item [renderer {:keys [value]}]
  (config! renderer :text (second value)))

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



(defn create-top-frame []
  (frame :title "Topoged",
         :size [500 :by 500]
         :on-close :exit
         ))


(defn file-menu [import-event-fn]
  (menu :text ::File
        :items [
                (action :handler #(future (import-event-fn %))
                        :name ::ImportGedcom
                        :tip ::ImportGedcomTip)
                (action :handler a-settings-handler
                        :name ::Settings
                        :tip ::SettingsTip)
                ]))

(defn create-container []
  (grid-panel
   :border ::Pedigree
   :items [(tabbed-panel
            :placement :top
            :tabs [ {:title ::Tree
                     :content pedigree-panel}
                    {:title ::Fractal
                     :content pedigree-panel-fractal}])]))

(defn build-individual-panel [id root-panel]
  (let [widget (grid-panel :columns 2) 
        m (to-data-map id)]
    (println m)
    (config! widget :items (mapcat (fn [[k v]] [(label (str k)) (label (str v))]) m))
    (config! root-panel :items nil)
    (config! root-panel :center  widget)))


(defn person-selection-handler [local-descendent-panel individual-panel e]
  (if (.getValueIsAdjusting e)
    
      (let [sel  (selection e)
            id (first sel)
            p-panel (future (build-pedigree-panel-tree id (border-panel) 1))
           ; p-panel  (build-pedigree-panel-tree id (border-panel) 1)
            i-panel  (build-individual-panel id (border-panel))
            f-panel (future (build-pedigree-panel-fractal id))
            d-panel (future (build-descendent-panel-tree id (border-panel) 1))]
        (def last-id id)
        (printf "e: %s sel: %s id: %s\n" e sel id)
        (config! local-descendent-panel :items [ @d-panel ])
        (config! pedigree-panel :items [ @p-panel ])
        (config! pedigree-panel-fractal :items [ @@f-panel ])
        (config! individual-panel :items [ i-panel])
        
)))


(defn frame-prepare [{:keys [locale db] :as topoged-context}]
  (let [status-bar (label :text "STATUS" :h-text-position :center)
        names-list-box (listbox :renderer render-name-item)
        local-descendent-panel (grid-panel :border ::Descendants)
        individual-panel (grid-panel :border ::Individual :columns 2)
        pedigree-panel-container (create-container)
        top-frame-container (border-panel
                             :size [500 :by 500]
                             :center (left-right-split
                                      (scrollable names-list-box)
                                      (left-right-split
                                       individual-panel
                                       (top-bottom-split
                                        pedigree-panel-container local-descendent-panel)))
                             :north (label :text "TOPOGED" :h-text-position :center)
                             :south status-bar)

        top-frame (create-top-frame)
        ]
    (letfn
        [
         (viewer-app []
           (try
             (listen names-list-box :selection #(future (handle-person-selection %)))
             
             (config! names-list-box :model (sort-by second (indi/individual-names db)))
             (config! top-frame :content top-frame-container)
             (config! top-frame :menubar (menubar :items [(file-menu menu-import-gedcom-handler)
                                                          (menu :text ::Reports :items [])
                                                          (menu :text ::Tasks   :items [])]))
             (-> top-frame
                 ;;pack!
                 show!)
             (catch Exception ex
               (.printStackTrace ex))))

         (menu-import-gedcom-handler [e]
           (try
             (let [tf top-frame-container
                   pi (create-plugin-info tf tf)]
               (gedcom-import-action topoged-context (assoc pi :status status-bar))
               (println (indi/individual-names db))
               (config! names-list-box :model (sort-by second (indi/individual-names db)))
               (println names-list-box))
             
             (catch Exception ex
               (.printStackTrace ex))))
         
         (handle-person-selection [e]
           (person-selection-handler local-descendent-panel individual-panel e))
         
         ]
      viewer-app)))

(defn -main [ & args ]
  (native!)
  (let [topoged-context (topoged-init)
        app (frame-prepare topoged-context)]
    (invoke-later (app))))


  


