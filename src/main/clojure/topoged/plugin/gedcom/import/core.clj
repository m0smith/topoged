(ns topoged.plugin.gedcom.import.core
  (:require [topoged.data.common :as db])
  (:use [topoged.file :only ( copy-md5)]
	[topoged.plugin.gedcom.import.fam :only (fam-handler)]
	[topoged.plugin.gedcom.import.util :only (add-source)]
	[topoged.plugin.gedcom.import.indi :only (indi-handler)]
	[topoged.service.plugin.ui :only (ui-choose-file ui-status)]
	[topoged gedcom]
    
    [clojure.java.io :only [input-stream output-stream reader]])
  (:import
   (java.io File InputStream OutputStream)
   (javax.swing JFileChooser JLabel)))

;;(set! *warn-on-reflection* true)

(defmulti handle-record (fn [_ __ rec] (:tag rec))) 

(remove-method handle-record :FAM)
(remove-method handle-record :INDI)
(remove-method handle-record :default)

(defmethod handle-record :FAM [sourceId process-state rec]
  (fam-handler sourceId process-state rec))

(defmethod handle-record :INDI [sourceId process-state rec]
  (indi-handler sourceId process-state rec))

(defmethod handle-record :default [sourceId process-state record]
  (println (str "skipping " (:tag record)))
  process-state)

(defn process-gedcom [sourceId gedseq]
  (reduce (partial handle-record sourceId) {} gedseq))

(defn import-gedcom [file]
  (let [tempfile (File/createTempFile "topoged-" ".ged")
        md5 (with-open [^InputStream  r (input-stream file)
                        ^OutputStream w (output-stream tempfile)]
              (copy-md5 r w))]
    (if (not-any? identity (db/entity :type db/source-type :md5 md5))
      (do
        (println (str "MD5 not found" md5))
        (let [source (db/add-source :source file
                                    :md5 md5
                                    :sourceType db/source-type-gedcom)
              gseq (gedcom-seq (line-seq (reader tempfile)))]
           (process-gedcom (:id source) gseq))))))

(defn gedcom-import-action [plugin-info]
  (if-let [ file (ui-choose-file plugin-info)]
    (import-gedcom file)))

	; ((persona-cause #(update-list-model list-model)))
	; ((persona-cause
	;   #(do (status-update uuid ["Imported" icnt fcnt scnt])
;		(status-complete uuid))))
