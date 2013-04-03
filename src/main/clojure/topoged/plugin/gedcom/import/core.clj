(ns topoged.plugin.gedcom.import.core
  (:require [topoged.data.common :as db]
            [topoged.type :as type])
  (:use [topoged.file :only (copy-md5)]
        [topoged.util :only (uuid)]
	[topoged.plugin.gedcom.import.head :only (head-handler subm-handler)]
	[topoged.plugin.gedcom.import.fam :only (fam-handler)]
	[topoged.plugin.gedcom.import.util :only (skip-handler handle-record)]
	[topoged.plugin.gedcom.import.indi :only (indi-handler)]
	[topoged.service.plugin.ui :only (ui-choose-file ui-status)]
	[topoged gedcom]
        [clj-time.core :only [now]]
    
        [clojure.java.io :only [input-stream output-stream reader]])
  (:import
   (java.io File InputStream OutputStream)
   (javax.swing JFileChooser JLabel)))

;;(set! *warn-on-reflection* true)

(def gedcom-well-known-types
  [
   [:gedcom-dest #uuid "e25ad0b0-88ca-11e2-9e96-0800200c9a66" "GEDCOM A system receiving data"
    "GEDCOM Destination" #uuid "c8bfb535-9bcb-40ad-b896-b635275722ad"]
   [:gedcom-source-version #uuid "e25ad0b1-88ca-11e2-9e96-0800200c9a66" "GEDCOM SOURCE Indicates which version of a product, item, or publication is being used or referenced"
    "GEDCOM Source Version" #uuid "eb1208de-a1dd-49f5-a744-5c14f2b955cd"]
   [:gedcom-source-name #uuid "e25ad0b2-88ca-11e2-9e96-0800200c9a66" "GEDCOM SOURCE NAME  A word or combination of words used to help identify an individual, title, or other item. More than one NAME line should be used for people who were known by multiple names"
    "GEDCOM Source Name" #uuid "3baa1b76-8b9e-479c-aa22-c6b0258f1fa2"]
   [:gedcom-source-corp #uuid "e25ad0b3-88ca-11e2-9e96-0800200c9a66" "GEDCOM SOURCE A name of an institution, agency, corporation, or company"
    "GEDCOM Source Corp" #uuid "5c6aa697-e5d8-4b9b-8b98-bbd790be2eec"]
   [:gedcom-source-address #uuid "e25ad0b4-88ca-11e2-9e96-0800200c9a66" "GEDCOM SOURCE The contemporary place, usually required for postal purposes, of an individual, a submitter of information, a repository, a business, a school, or a company."
    "GEDCOM Source Address" #uuid "56603543-7495-4bc0-adf4-b6d241ab6267"]
   [:gedcom-version #uuid "e25ad0b5-88ca-11e2-9e96-0800200c9a66" "GEDCOM Indicates which version of a product, item, or publication is being used or referenced"
    "GEDCOM Version" #uuid "98a5985e-cfc7-43bd-b013-12fde983121d"]
   [:gedcom-form #uuid "e25ad0b6-88ca-11e2-9e96-0800200c9a66" "GEDCOM An assigned name given to a consistent format in which information can be conveyed"
    "GEDCOM Form" #uuid "0f35ce96-f1da-497a-82c4-442b8b3422fd"]
   [:gedcom-source #uuid "e25ad0b7-88ca-11e2-9e96-0800200c9a66" "GEDCOM SOURCE The initial or original material from which information was obtained."
    "GEDCOM Source" #uuid "bbf61d6e-7228-4969-bedc-c3d98e9a7280"]
   [ :gedcom #uuid "e25ad0b8-88ca-11e2-9e96-0800200c9a66" "GEDCOM Source Type" "GEDCOM" #uuid "22c8cc48-bce7-4675-929b-107d3ab2e986" ]
   [ :gedcom-status #uuid "e25ad0ba-88ca-11e2-9e96-0800200c9a66" "Status from a GEDCOM record" "Status" #uuid "9dff005e-d5c6-4183-a49a-122db38cfd41" ]
   
   ])
;
;
;#uuid 
;
;#uuid e25ad0bb-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0bc-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0bd-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0be-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0bf-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c0-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c1-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c2-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c3-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c4-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c5-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c6-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c7-88ca-11e2-9e96-0800200c9a66
;#uuid e25ad0c8-88ca-11e2-9e96-0800200c9a66


(def zero-level-handlers
  {
   :HEAD head-handler
   :SUBM subm-handler
   :INDI indi-handler
;   :FAM fam-handler
   })


(defn process-gedcom [process-state gedseq]
  (reduce (partial handle-record zero-level-handlers skip-handler) process-state gedseq))

(defn new-gedcom-source []
  {
   :id (uuid)
   :key :source-document
   (db/key-id :citationType) (db/key-id :web-based-media)
   (db/key-id :mediaType) (db/key-id :gedcom)
   (db/key-id :city) ""
   (db/key-id :accessedDate ) (now)
   (db/key-id :mediumTypeId) (db/key-id :web-medium)
   }
  )

(defn new-gedcom-attachment [source md5 content]
  (merge (db/new-attachment-document)
         {
          :sourceId (:id source)
          :md5 md5
          :content content
          :type (db/key-id :gedcom)
          }))

(defn init-process-state
  []  {
       :source (new-gedcom-source)
       :path []
       :id-in-source {}
       })

(defn import-gedcom [file]
  (let [tempfile (File/createTempFile "topoged-" ".ged")
        md5 (with-open [^InputStream  r (input-stream file)
                        ^OutputStream w (output-stream tempfile)]
              (copy-md5 r w))]
    (if (not-any? identity (db/entity :key :attachment-document :md5 md5))
      (do
        (println (str "MD5 not found" md5))
        (let [process-state (init-process-state)
              source (:source process-state)
              attachment (new-gedcom-attachment source md5 (slurp file))
              gseq (gedcom-seq (line-seq (reader tempfile)))]
          (process-gedcom process-state gseq))))))

(defn gedcom-import-action [plugin-info]
  (if-let [ file (ui-choose-file plugin-info)]
    (import-gedcom file)))

	; ((persona-cause #(update-list-model list-model)))
	; ((persona-cause
	;   #(do (status-update uuid ["Imported" icnt fcnt scnt])
;		(status-complete uuid))))

(defn init-plugin []
  (doseq [well-known-type gedcom-well-known-types] (type/ensure-well-known-type well-known-type)))