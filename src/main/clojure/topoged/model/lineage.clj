(ns topoged.model.lineage
    (:use [topoged db])
  (:require [archimedes.vertex :as v]
            [archimedes.edge :as e]
            [archimedes.query  :as q]))

(defn lineage-of [node direction]
  (sort-by (comp :order to-data-map)
           (q/find-edges node
                         (q/direction direction)
                         (q/labels :lineage))))
(defn parents-of [node]
  (map e/head-vertex  (lineage-of node :out)))

(defn children-of [node]
  (map e/tail-vertex  (lineage-of node :in)))
