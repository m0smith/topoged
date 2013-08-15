(ns topoged.plugin.gedcom.import.import
  (:require [archimedes.edge :as e]
            [archimedes.core :as g]
            [archimedes.vertex :as v]
            [archimedes.query  :as q])
  (:use [topoged.file :only (copy-to-temp )]
        [topoged gedcom db]
        [topoged.plugin.gedcom.import.util :only (using-default-handler handle-record skip-handler)]
        [topoged.plugin.gedcom.import.head :only (head-handler subm-handler)]
        [clojure.java.io :only [input-stream output-stream reader]]
        [clj-time.core :only [now]]))

(def ^:dynamic *researcher* nil)
(def ^:dynamic *type* nil)

(defn already-imported? 
  "Returns nil if not already imported or a seq of the delta vertices of the
  previously imported ones."  
  [temp-file md5]
  (if-let [medias (seq (v/find-by-kv :md5 md5))]
    (let [sources (q/find-vertices (first medias) (q/direction :in))]
      (q/find-vertices (first sources ) 
                       (q/direction :in)
                       (q/labels :what)))))
        
  

(defn init []
  (g/use-clean-graph!)
  (alter-var-root (var *researcher*)
                  (fn [r] (v/create! {:type :researcher})))
  (alter-var-root (var *type*)
                  (fn [r] (v/create! {:type :process :name :gedcom-importer :version 1.0 }))))


(defn initialize-source 
  "Take the GEDCOM and the md5 and create the graph that relates the
  research, media and source all together.  Return [source delta].

  NOTE: researcher can be specified by binding the *researcher* var"
  [input md5]
  (let [now (now)
        source (v/create! {:type :source :media :gedcom 
                           :meduim :web :accessed-date now} )
        media (v/create! {:type :media :md5 md5 :media :gedcom :data (slurp input)} )
        delta (v/create! {:type :delta :date now} )]
    (add-edge source :attachment media)    
    ;(add-edge source :contributor *type*  )
    (add-edge delta :using *type* )
    (add-edge delta :who *researcher* )
    (add-edge delta :basis source )
    [source delta media]))

(def zero-level-handlers 
  (using-default-handler skip-handler 
                 {:HEAD head-handler
                  :SUBM subm-handler}))

(defn import-gedcom1 [input md5]
  (let [[source delta media] (initialize-source input md5)
        gseq (gedcom-seq (line-seq (reader input)))
        process-state {:source source :media media}]
    (reduce #(handle-record zero-level-handlers %1 %2 []) process-state gseq)
    [source delta media]))
 
(defn import-gedcom [input]
  "Imports a gedcom. Expects an something reader likes.  Returns [delta]"
  (let [[temp-file md5] (copy-to-temp "topoged-" ".ged" input)]
    (if-let [delta (already-imported? temp-file md5)]
      delta
      (import-gedcom1 temp-file md5))))
