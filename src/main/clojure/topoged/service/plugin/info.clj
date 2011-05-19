(ns topoged.service.plugin.info
  (:import
   (javax.swing  JFileChooser)))

(defn create-plugin-info [frame]
  " :frame - JFrame - the outer frame for the application
    :fileChooser - JFileChooser - the dialog box for the user to select files on disk
    :status - JPanel - the status bar"
  {:frame frame
   :fileChooser (JFileChooser. ".")})