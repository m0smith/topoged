(ns topoged.viewer.common
  (:use [seesaw core tree graphics])
  (:require [topoged.data.common :as db]))

(def m-entity (memoize (partial db/entity :id)))
(def m-parents-of (memoize db/parents-of))
(def m-children-of (memoize db/children-of))

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
  (map #(if (nil? %) db/UNDEFINEDX %) coll))


(defn load-model [id next-gen-fn]
  ;(println "LOAD-MODEL:" id)
  (let [rtnval (simple-tree-model identity (comp map-undef next-gen-fn) id)]
    ;(println "RTNVAL:" rtnval)
    rtnval))


(defn render-fn [renderer info]
  (let [ent (first (m-entity (:value info)))]
    ;(println "ENTITY:" ent info)
    (config! renderer
             :icon  (icon-sex (:sex ent))
             :text (:name ent))))