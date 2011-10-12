(ns topoged.test.entity
  (:use [ clojure.java.io :only [reader]])
  (:use [ topoged.gedcom :only [gedcom-seq]]))

(defn process-gedcom [f]
  (with-open [rdr (reader f)]
    (doseq [line (gedcom-seq rdr)]
      (println (:tag line)))))


