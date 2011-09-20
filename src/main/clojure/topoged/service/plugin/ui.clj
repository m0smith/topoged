(ns topoged.service.plugin.ui
    (:import
     (javax.swing JFileChooser JLabel)))

(defn ui-choose-file [plugin-info]
  (let [^JFileChooser fc ( :fileChooser plugin-info)] 
    (. fc showOpenDialog (:frame plugin-info))
    (.getSelectedFile fc)))

(defn ui-status [plugin-info] (:status plugin-info))

