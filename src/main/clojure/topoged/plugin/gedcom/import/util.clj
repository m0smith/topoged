(ns topoged.plugin.gedcom.import.util
  ;(:require [topoged.data.common :as db]
  ;          [archimedes.vertex :as v])
  ;(:use [topoged.gedoverse :only (add group-agent)]
  ;      [topoged db])
)



(defn skip-handler [ {{:keys [path]} :local-context :as import-context}
                     {:keys [tag]  :as record}
                     path
                     ]
  (println "Skipping " path " tag " tag)
  import-context)


(defn apply-h [ & handlers]
  (fn [import-context record path]
    (reduce #(%2 %1 record path) import-context handlers)))


(defn handle-record-with-default [default-handler
                                  handler-map
                                  path
                                  import-context
                                  {:keys [tag] :as record}]
  (let [h (get handler-map tag default-handler)]
    (h import-context record path)))



(defn nested-handler* [handler-map 
                       import-context 
                       {:keys [tag content] :as record} 
                       path]
  (let [fun (partial handle-record-with-default skip-handler handler-map (conj path tag))]
    (reduce fun import-context content)))



