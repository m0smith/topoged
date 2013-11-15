(ns topoged.model.individual
    (:use [topoged db])
  (:require [archimedes.vertex :as v]
            [archimedes.edge :as e]
            [archimedes.query  :as q]))

(defn node-name-pair [node]
  (let [m (to-data-map node)]
    [node (:name m)]))


(defn individual-names 
  "Return a seq of pairs of [vertex name]"
  [db]
  (sort-by second
           (map node-name-pair (find-by-kv db :type :individual))))
