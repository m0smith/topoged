(ns topoged.test.entity
  (:use [ clojure.java.io :only [reader]])
  (:use [ topoged.gedcom :only [gedcom-seq]]))

(def dispatch {
	       :HEAD #(println (:tag %))
	       :SUBM #(println (:tag %))
	       :INDI #(println (:tag %))
	       :FAM #(println (:tag %))
	       :TRLR #(println (:tag %))
	       })


(defn conjv
  ( [coll x] (conj (if coll coll #{}) x))
  ( [coll x & xs]
      (if xs
	(recur (conjv coll x) (first xs) (rest xs))
	(conjv coll x)))
  )

(defstruct topoged-type :id :name)
(defstruct source :id :type-id)


(defn update-state [state key value]
  (update-in state [key] conjv value))


(defn find-type [state name]
  (let [types (:type state)]
    (first (filter #(= (:id %) name) types))))


(defmulti handler (fn [rtnval rec] (:tag rec)))
(defmethod handler :HEAD [rtnval rec]
	   (let [atype (struct topoged-type "GEDCOM" "GEDCOM")
		 asource (struct source "GEDCOM" (:id atype))]
	     (-> rtnval
		 (update-state :type atype)
		 (update-state :source asource))))

(defmethod handler :SUBM [rtnval rec] (println (:tag rec)) rtnval)
(defmethod handler :INDI [rtnval rec] (println (:tag rec)) rtnval)
(defmethod handler :FAM [rtnval rec] (println (:tag rec)) rtnval)
(defmethod handler :TRLR [rtnval rec] (println (:tag rec)) rtnval)
(defmethod handler :default [rtnval rec] (println "ERROR" (:tag rec))rtnval)


(defn process-gedcom [f]
  (with-open [rdr (reader f)]
    (reduce handler {}  (gedcom-seq rdr))))


