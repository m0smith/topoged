(ns q
  (:use [topoged.db]
        [topoged.init :only [topoged-init]]
        [topoged.model.individual :only [individual-names]]
        [topoged.model.lineage :only [parents-of]]
        [topoged.plugin.gedcom.import.import]))

(defn q [topoged-context] (import-gedcom topoged-context "src/test/resources/gedcom/WJTHOMAS.ged"))

(defn q2 []
  (let [{:keys [db] :as topoged-context} (topoged-init)]
    ;(init topoged-context)
    (q topoged-context)
    (let [people (individual-names db)
          parents (parents-of (first (last people)))]
      (map to-data-map parents))))

