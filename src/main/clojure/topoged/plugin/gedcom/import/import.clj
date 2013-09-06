(ns topoged.plugin.gedcom.import.import
  (:require [archimedes.vertex :as v]
            [archimedes.query  :as q])
  (:use [topoged.file :only (copy-to-temp)]
        ;[topoged gedcom init]
        [topoged.db]
        [topoged.gedcom :only (gedcom-seq)]
        [topoged.plugin.gedcom.import.util :only (handle-record-with-default skip-handler)]
        [topoged.plugin.gedcom.import.head :only (head-handler2 
                                                  subm-nested-handler)]
        [topoged.plugin.gedcom.import.indi :only (indi-handler)]
        [clojure.java.io :only [input-stream output-stream reader]]
        [clj-time.core :only [now]]))


(defrecord SharedContext [source delta media input md5])
(defrecord ImportContext [shared-context local-context db user])

(defn already-imported? 
  "Returns nil if not already imported or a seq of the delta vertices of the
  previously imported ones."  
  [temp-file md5]
  (if-let [medias (seq (v/find-by-kv :md5 md5))]
    (let [sources (q/find-vertices (first medias) (q/direction :in))]
      (q/find-vertices (first sources ) 
                       (q/direction :in)
                       (q/labels :what)))))

(defn initialize-context 
  "Take the GEDCOM and the md5 and create the graph that relates the
  research, media and source all together. "
  [db user type input md5]
  (let [now (now)
        source (add-node db {:type :source :media :gedcom 
                           :meduim :web :accessed-date now} )
        media (add-node db {:type :media :md5 md5 :media :gedcom :data (slurp input)} )
        delta (add-node db {:type :delta :date now} )]
    (add-edge db source :attachment media {})    
    (add-edge db source :contributor type {}  )
    (add-edge db delta  :using type {})
    (add-edge db delta  :who user {})
    (add-edge db delta  :basis source {})
    (->ImportContext
     (->SharedContext  source  delta  media  input  md5)
     {} db user)))

(def top-level-handler-map
  {:HEAD head-handler2
   :SUBM subm-nested-handler
   :INDI indi-handler})


(defn prepare [{:keys [user db] :as topoged-context}]
  (let [type  (add-node db {:type :process :name :gedcom-importer :version 1.0})]
    (letfn 
        [(import-gedcom* [input]
           (let [[temp-file md5] (copy-to-temp "topoged-" ".ged" input)]
             (if-let [delta (already-imported? temp-file md5)]
               delta
               (let [import-context (initialize-context db user type temp-file md5)
                     
                     gseq (gedcom-seq (line-seq (reader input)))]
                 (reduce (partial handle-record-with-default skip-handler top-level-handler-map []) import-context gseq)))))]
      import-gedcom*)))

(defn import-gedcom [topoged-context input]
  (let [importer (prepare topoged-context)]
    (importer input)))
