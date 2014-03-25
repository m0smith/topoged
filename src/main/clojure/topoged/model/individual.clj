(ns topoged.model.individual
    (:use [topoged db]
          [topoged.data path schema])
  (:require [archimedes.vertex :as v]
            [archimedes.edge :as e]
            [archimedes.query  :as q]))

(defn node-name-pair [node]
    [node (:name (to-data-map node))])


(defn individual-names 
  "Return a seq of pairs of [vertex name]"
  [db]
  (sort-by second
           (map (comp node-name-pair first) (path-query [Individual]))))
