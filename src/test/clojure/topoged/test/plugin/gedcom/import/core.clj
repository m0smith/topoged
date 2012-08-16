(ns topoged.test.plugin.gedcom.import.core
  
  (:use
   [topoged.plugin.gedcom.import.core :only [import-gedcom]]
   [clojure.test :only [run-tests deftest is]]))


(def ged1 "src/test/resources/simple.ged")
(def ged2 "src/test/resources/TiberiusClaudiusCaesarAugustusGermanicusClaudiusEmperorofRome.ged")

(deftest testit []
  (import-gedcom ged1 (java.util.UUID/randomUUID) (agent {}))
  (send topoged.gedoverse/source-agent (fn [x] {})))