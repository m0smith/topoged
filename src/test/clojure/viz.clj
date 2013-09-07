(ns viz
  (:import [com.tinkerpop.blueprints.oupls.jung GraphJung]
           [edu.uci.ics.jung.algorithms.layout CircleLayout RadialTreeLayout]
           [edu.uci.ics.jung.visualization VisualizationViewer BasicVisualizationServer]
           [java.awt Dimension]
           [org.apache.commons.collections15 Transformer Predicate])
  (:require [archimedes.core :as g])
  (:use [q]
        seesaw.core
        ))

(defn to-int [s]
  (Integer/parseInt s))

(defn -main [& args]
  (q2)
  (let [graph (GraphJung. g/*graph*)
        layout (doto (CircleLayout. graph)
                 (.setSize(Dimension. 800 800)))
        viz (doto (BasicVisualizationServer. layout)
              (.setPreferredSize (Dimension. 850 850)))
        rc (.getRenderContext viz)]
    (doto rc
      (.setEdgeLabelTransformer (reify Transformer 
                                  (transform [_ edge] 
                                    (.getLabel edge))))
      (.setVertexIncludePredicate (reify Predicate
                                    (evaluate [_ context]
                                      (let [id (to-int (.getId (. context element)))]
                                        (<  id  20)))))
      (.setVertexLabelTransformer (reify Transformer 
                                    (transform [_ vertex] 
                                      (str (.getProperty vertex "name"))))))
      
    
    (invoke-later

       (-> (frame :title "Imported GEDCOM",
                  :content viz, 
                  :on-close :dispose)
           
           pack!
           show!))))

