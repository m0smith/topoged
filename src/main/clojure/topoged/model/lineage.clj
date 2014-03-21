(ns topoged.model.lineage
    (:use [topoged db])
  (:require [archimedes.vertex :as v]
            [archimedes.edge :as e]
            [archimedes.query  :as q]))


(defn group-of 
  ([node]
     (map e/head-vertex (q/find-edges node
                                      (q/direction :in))))
  ([node label]
     (println "group-of: " node label)
     (map e/head-vertex (q/find-edges node
                                      (q/direction :in)
                                      (q/labels label)))))

(defn parents-of [node]
  (println "parents-of:" node)
  (let [rtnval (map e/tail-vertex (q/find-edges node
                                     (q/direction :in)
                                     (q/labels :child)))]
    (println "parents-of rtnval:" rtnval)
    rtnval))

(defn children-of [node]
  (map e/head-vertex (q/find-edges node
                        (q/direction :out)
                        (q/labels :child))))

