(ns q
  (:require [archimedes.vertex :as v])
  (:use [topoged.db]
        [topoged.viewer frame]
        [seesaw core graphics tree]
        [topoged.init :only [topoged-init]]
        [topoged.model.individual :only [individual-names]]
        [topoged.plugin.gedcom.import.import]))

(defn q [topoged-context] (import-gedcom topoged-context "src/test/resources/gedcom/WJTHOMAS.ged"))

(defn q2 []
  (let [{:keys [db] :as topoged-context1} (topoged-init)]
    (def topoged-context topoged-context1)
    (q topoged-context1)
    (let [people (individual-names db)
          person (-> people last first)]
      people)))

(defn q3 []
  "maybe try (invoke-later ((q3)))"
  (def topoged-context (topoged-init))
  (def app             (frame-prepare topoged-context))
  app)
