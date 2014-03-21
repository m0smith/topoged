(ns topoged.test.plugin.gedcom.import.core
  
  (:use
   [topoged.plugin.gedcom.import.core :only [import-gedcom]]
   [topoged.util :only [uuid]]
   [clojure.test :only [run-tests deftest is]]))


(def ged1 "src/test/resources/simple.ged")
(def ged2 "src/test/resources/TiberiusClaudiusCaesarAugustusGermanicusClaudiusEmperorofRome.ged")

(deftest testit []
  (import-gedcom ged1 (uuid) (agent ["Importing" 0 0 0] ) (agent {}) )
)