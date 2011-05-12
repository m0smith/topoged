(ns topoged.plugin.gedcom.import.core
  (:use [topoged.file :only (input-stream output-stream copy-md5)]
	[topoged.plugin.gedcom.import.fam :only (fam-handler)]
	[topoged.plugin.gedcom.import.indi :only (indi-handler)]
	[topoged.gedoverse]
	[topoged.gedcom])
  (:import
   (java.io File InputStream OutputStream)))

(set! *warn-on-reflection* true)

(defmulti handle-record (fn [_ rec __] (:tag rec))) 

(defmethod handle-record :FAM [uuid rec status]
	   (fam-handler [uuid rec status]))
(defmethod handle-record :INDI [uuid rec status]
	   (indi-handler [uuid rec status]))
(defmethod handle-record :default [uuid record status] record)

(defn process-gedcom [uuid gedseq status-agent]
  (doall (map #(handle-record uuid % status-agent) gedseq)))
      
    


(defn import-gedcom [file uuid status-agent]
  (let [tempfile (File/createTempFile "topoged-" ".ged")
	md5 (with-open [^InputStream r (input-stream file)
			^OutputStream w (output-stream tempfile)]
	      (copy-md5 r w))]
    (if (not-empty (filter #(= md5 (:md5 %)) (vals @source-agent)))
      (do
	(log (str "Already imported" file))
	(send status-agent (fn [_] ["Aborted" 0 0 0])))
      (do
	(log (str "MD5 not found" md5))
	(send source-agent add {:id uuid
				:source file
				:md5 md5}
	      #(send status-agent (fn [f] (assoc f 3 (inc (f 3))))))
	(process-gedcom uuid (gedcom-seq tempfile) status-agent)))))


(defn gedcom-import-action [^JFileChooser fc frame status-panel update-list-model list-model]
  (. fc showOpenDialog frame)
  (if-let [ file (.getSelectedFile fc)]
    (future
     (let [uuid (str (java.util.UUID/randomUUID))
	   label (JLabel.)
	   status-agent (agent ["Importing" 0 0 0])]
       (.add status-panel label)
       (add-watch status-agent :panel
		  #(.setText label (format "%s  I:%,d F:%,d S:%,d" %4)))
       (import-gedcom file uuid status-agent)
	 ((persona-cause #(update-list-model list-model)))
	 ((persona-cause
	   #(do (status-update uuid ["Imported" icnt fcnt scnt])
		(status-complete uuid))))
