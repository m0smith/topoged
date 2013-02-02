(ns topoged.viewer.pedigree.tree
  (:use [seesaw core graphics tree]
        [topoged.viewer.common]))


(def pedigree-panel (grid-panel))


(defn build-pedigree-panel-tree "return root-panel"
  [id root-panel offset]
  ;(println "TREE:" root-panel)
  
  (let [widget (tree :id :tree
                     :model (load-model id m-parents-of)
                     :renderer render-fn)]
    ;(println "WIDGET:" (class widget))
    (config! root-panel :items nil)
    (config! root-panel :center (scrollable widget))
    (expand-children widget 32)
    ;(println "TREE END:" root-panel)
    )
  
  root-panel)



