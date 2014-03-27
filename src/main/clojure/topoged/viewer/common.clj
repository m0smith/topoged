(ns topoged.viewer.common
  (:use [seesaw core tree graphics]
        [topoged db][topoged.data path schema])
  (:require [topoged.model.lineage :as lineage]))


;;(def UNDEFINEDX #uuid "a4d6c4d6-bb29-45ca-8bf6-25c06168a8d5")
(def UNDEFINEDX {})

;(def m-entities (memoize (partial db/entities :id)))
(defn m-entities [ arg ]
  [(to-data-map arg)])

(def m-parents-of  parents-of)
(def m-children-of (memoize children-of))

(defn std-sex [sex]
  (cond
   (#{"M" :male} sex) :male
   (#{"F" :female} sex) :female
   :else :unknown))


(def icon-sex-dict
  {:male "image/male16.png"
   :female "image/female16.png"
   :unknown "image/question_octagon16.png"})

(defn icon-sex [sex]
  (icon-sex-dict (std-sex sex)))



(defn expand-children [jtree levels]
  (dotimes [j levels]
    (println "expand-children:" j jtree levels)
    (let [paths (seq (.getPathBetweenRows jtree 0 levels))]
    (println "expand-children: paths" paths)
      
      (doseq [path paths]
        (println "expand-children: path" path)
        (.expandPath jtree path)))))


(defn map-undef [coll]
  (map #(if (nil? %) UNDEFINEDX %) coll))


(defn model-next-gen-fn [f]
  (fn [n]
    (if (not= UNDEFINEDX n)
      (map-undef (f n)))))

(defn load-model [id next-gen-fn]
  ;(println "LOAD-MODEL:" id)
  (let [rtnval (simple-tree-model identity (model-next-gen-fn next-gen-fn) id)]
    ;(println "RTNVAL:" rtnval)
    rtnval))


(defn render-fn [renderer {:keys [value]}]
  (println "render-fn:" value)
  (let [me (m-entities value)
        {:keys [sex name] :as ent}  (first me)]
    
    (println "ENTITY:" me name sex ent)
    (config! renderer
             :icon  (icon-sex sex)
             :text name)))
