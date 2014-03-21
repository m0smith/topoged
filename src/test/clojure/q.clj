(ns q
  (:require [archimedes.vertex :as v])
  (:use [topoged.db]
        [topoged.viewer frame]
        [seesaw core graphics tree]
        [topoged.init :only [topoged-init]]
        [topoged.model.individual :only [individual-names]]
        [topoged.model.lineage :only [parents-of children-of]]
        [topoged.plugin.gedcom.import.import]))

(defn q [topoged-context] (import-gedcom topoged-context "src/test/resources/gedcom/WJTHOMAS.ged"))

(defn q2 []
  (let [{:keys [db] :as topoged-context} (topoged-init)]
    (q topoged-context)
    (let [people (individual-names db)
          person (-> people last first)]
      [ (to-data-map person)
        (seq (v/all-edges-of person)) ]
)))

(defn q3 []
  (def topoged-context (topoged-init))
  (def app             (frame-prepare topoged-context))
  app)
