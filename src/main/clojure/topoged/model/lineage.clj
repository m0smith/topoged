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
  (let [groups (group-of node :child)]
    (println "parents-of: groups" groups)
    (map e/head-vertex (q/find-edges node
                                     (q/direction :out)
                                     (q/labels :parent)))))

(defn children-of [node]
  (let [groups (group-of node :parent)]
    (map e/head-vertex (q/find-edges node
                                     (q/direction :out)
                                     (q/labels :child)))))

