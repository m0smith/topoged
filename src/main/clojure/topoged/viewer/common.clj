(ns topoged.viewer.common
  (:use [seesaw core tree graphics]
        [topoged db])
  (:require [topoged.model.lineage :as lineage]))


(def UNDEFINEDX #uuid "a4d6c4d6-bb29-45ca-8bf6-25c06168a8d5")

;(def m-entities (memoize (partial db/entities :id)))
(defn m-entities [ arg ]
  [(to-data-map arg)])

(def m-parents-of (memoize lineage/parents-of))
(def m-children-of (memoize lineage/children-of))

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
    (let [paths (seq (.getPathBetweenRows jtree 0 levels))]
      
      (doseq [path paths]
        (.expandPath jtree path)))))


(defn map-undef [coll]
  (map #(if (nil? %) UNDEFINEDX %) coll))


(defn load-model [id next-gen-fn]
  (println "LOAD-MODEL:" id)
  (let [rtnval (simple-tree-model identity (comp map-undef next-gen-fn) id)]
    (println "RTNVAL:" rtnval)
    rtnval))


(defn render-fn [renderer {:keys [value]}]
  ;;(println "render-fn:" value)
  (let [{:keys [sex name] :as ent}  (first (m-entities value))]
    ;;(println "ENTITY:" name sex ent)
    (config! renderer
             :icon  (icon-sex sex)
             :text name)))
