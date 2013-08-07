(ns topoged.plugin.gedcom.import.import
  (:require [archimedes.edge :as e]
            [archimedes.core :as g]
            [archimedes.vertex :as v]
            [archimedes.query  :as q])
  (:use [topoged.file :only (copy-to-temp )]
        [clj-time.core :only [now]]))

(def ^:dynamic *researcher* nil)
(def ^:dynamic *type* nil)

(def add-edge (partial e/connect-with-id! nil))

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
        media (v/create! {:type :media :md5 md5 :media :gedcom} )
        delta (v/create! {:type :delta } )]
    (add-edge source :attachment media)    
    (add-edge source :contributor *type* {:date now} )
    (add-edge delta :using *type* )
    (add-edge delta :who *researcher* )
    (add-edge delta :what source )
    [source delta]))

(defn import-gedcom1 [input md5]
  (let [[source delta] (initialize-source input md5)]
    [delta]))

(defn import-gedcom [input]
  "Imports a gedcom. Expects an something reader likes.  Returns [delta]"
  (let [[temp-file md5] (copy-to-temp "topoged-" ".ged" input)]
    (if-let [delta (already-imported? temp-file md5)]
      delta
      (import-gedcom1 temp-file md5))))
