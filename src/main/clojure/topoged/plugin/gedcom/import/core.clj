(ns topoged.plugin.gedcom.import.core
  (:use [topoged.file :only ( copy-md5)]
	[topoged.plugin.gedcom.import.fam :only (fam-handler)]
	[topoged.plugin.gedcom.import.util :only (add-source)]
	[topoged.plugin.gedcom.import.indi :only (indi-handler)]
	[topoged.gedoverse]
	[topoged.service.plugin.ui :only (ui-choose-file ui-status)]
	[topoged.gedcom]
    [clojure.java.io :only [input-stream output-stream reader]])
  (:import
   (java.io File InputStream OutputStream)
   (javax.swing JFileChooser JLabel)))

(set! *warn-on-reflection* true)

(defmulti handle-record (fn [_ rec __] (:tag rec))) 

(remove-method handle-record :FAM)
(remove-method handle-record :INDI)
(remove-method handle-record :default)

(defmethod handle-record :FAM [uuid rec status]
	   (fam-handler uuid rec status))
(defmethod handle-record :INDI [uuid rec status]
	   (do  (indi-handler uuid rec status)))
(defmethod handle-record :default [uuid record status] (println (str "skipping " (:tag record))) record)

(defn process-gedcom [uuid gedseq status-agent]
  (doall (map #(handle-record uuid % status-agent) gedseq)))



(defn import-gedcom [file uuid status-agent source-agent]
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
        (send source-agent add-source {:id uuid
                                       :source file
                                       :md5 md5} status-agent)
        (let [gseq (gedcom-seq (line-seq (reader tempfile)))]
          (process-gedcom uuid gseq status-agent)
          (await persona-agent source-agent group-agent)
          (send status-agent (fn [f] (assoc f 0 "Completed")))))))) 



(defn gedcom-import-action [plugin-info]
  (if-let [ file (ui-choose-file plugin-info)]

     (let [uuid (str (java.util.UUID/randomUUID))
	   label (JLabel.)
	   status-agent (agent ["Importing" 0 0 0])]
       (.add ^JLabel (ui-status plugin-info) label)
       (add-watch status-agent :panel
		  #(.setText label (apply format (cons "%s  I:%,d F:%,d S:%,d" %4))))
		  ;;#(.setText label (format "%s  I:%,d F:%,d S:%,d" %4)))
       (import-gedcom file uuid status-agent source-agent))))

	; ((persona-cause #(update-list-model list-model)))
	; ((persona-cause
	;   #(do (status-update uuid ["Imported" icnt fcnt scnt])
;		(status-complete uuid))))
