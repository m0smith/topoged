(ns topoged.viewer.seesaw
  (:gen-class)
  (:import [javax.swing UIManager SwingUtilities])
  (:require [seesaw.core :as ss]))


(defn -main [ & args]
  ;;(ss/native!)
  (UIManager/setLookAndFeel (UIManager/getSystemLookAndFeelClassName))
  (ss/invoke-later
   (let [f (ss/frame :title "test" :content "content")]
     (SwingUtilities/updateComponentTreeUI f)
     (ss/pack! f)
     (ss/show! f)
     f)))
  
  
