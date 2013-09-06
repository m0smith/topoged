(ns q
  (:use [topoged.init])
  (:use [topoged.plugin.gedcom.import.import]))

(defn q [topoged-context] (import-gedcom topoged-context "src/test/resources/gedcom/WJTHOMAS.ged"))

(defn q2 []
  (let [topoged-context (topoged-init)]
    ;(init topoged-context)
    (q topoged-context)))
