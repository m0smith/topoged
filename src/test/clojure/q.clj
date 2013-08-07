

(ns q
  (:use [topoged.init])
  (:use [topoged.plugin.gedcom.import.import]))

(defn q [] (import-gedcom "src/test/resources/gedcom/WJTHOMAS.ged"))
(defn q2 []
  (topoged-init)
  (init)
  (q))
