(ns test
  (:use [clojure.java.io :only [reader]])
  (:use [clojure.test :only [run-tests deftest is]])
  (:use [topoged.gedcom :only [gedcom? gedcom-seq]])
  )

(def ged1 "src/test/resources/simple.ged")
(def ged2 "src/test/resources/TiberiusClaudiusCaesarAugustusGermanicusClaudiusEmperorofRome.ged")

(def geds "0 HEAD\n0 @FATHER@ INDI\n1 NAME /Father/")

(deftest gedcom?-test []
         (is (gedcom? geds))
         (is (not (gedcom? ged1)))
         )

(deftest gedcom-seq-on-string-test []
	 (let [s (re-seq #".*" geds)]
	   (prn s)
	   (is (= 2 (count (gedcom-seq  s))))))

(deftest gedcom-seq-on-file-test []
         (let [gseq (gedcom-seq (line-seq (reader ged1)))]
           (is (= 7 (count gseq)))
           (is (= "/Father/" (-> (nth gseq 2) :content first :value)))))

(deftest gedcom2 []
  (let [gseq (gedcom-seq (line-seq (reader ged2)))]
           (is (= 89 (count gseq)))
           (is (= "Tiberius Claudius Caesar Augustus Germanicus /Claudius/ Emperor of Rome" (-> (nth gseq 2) :content first :value))))
  )
(deftest model-id [])


