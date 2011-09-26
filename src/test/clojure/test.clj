(ns test
  (:use [clojure.test :only [run-tests deftest is]])
  (:use [topoged.gedcom :only [gedcom? gedcom-seq]])
  )

(def ged1 "src/test/resources/simple.ged")
(def geds "0 HEAD\n0 @FATHER@ INDI\n1 NAME /Father/")

(deftest gedcom?-test []
         (is (gedcom? geds))
         (is (not (gedcom? ged1)))
         )

(deftest gedcom-seq-test []
         (let [gseq (gedcom-seq ged1)]
           (is (= 2 (count (gedcom-seq geds))))
           (is (= 7 (count gseq)))
           (is (= "/Father/" (-> (nth gseq 2) :content first :value)))))



