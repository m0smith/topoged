(ns topoged.test.entity
  (:use [clojure.java.io :only [reader writer input-stream output-stream]]
	[topoged.file :only (copy-md5)]
	[topoged.gedcom :only [gedcom-seq]]))

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

(defstruct TYPE :TYPE_ID :TYPE_NAME)
(defstruct SOURCE :SOURCE_ID :TYPE_ID)
(defstruct REPRESENTAITON :SOURCE_ID :TYPE_ID :CONTENT :COMMENTS)


(defn update-state [state key value]
  (update-in state [key] conjv value))


(defn find-type [state name]
  (let [types (:type state)]
    (first (filter #(= (:id %) name) types))))


(defmulti handler (fn [rtnval rec] (:tag rec)))
(defmethod handler :HEAD [state rec] state)

(defmethod handler :SUBM [rtnval rec] (println (:tag rec)) rtnval)
(defmethod handler :INDI [rtnval rec] (println (:tag rec)) rtnval)
(defmethod handler :FAM [rtnval rec] (println (:tag rec)) rtnval)
(defmethod handler :TRLR [rtnval rec] (println (:tag rec)) rtnval)
(defmethod handler :default [rtnval rec] (println "ERROR" (:tag rec))rtnval)

(def initial-state
     {
      :type, #{
	       (struct TYPE "GEDCOM" "GEDCOM")
	       (struct TYPE "MD5" "MD5")
	       }
      :source, #{
		(struct SOURCE "GEDCOM" "GEDCOM")
		}
      })

(defn process-gedcom [f]
  (let [out-name "/tmp/f.ged"
	md5 (let [in  f
		  out out-name]
	      (copy-md5 in out))
	state (merge initial-state
		     {:representation
		      #{
			(struct REPRESENTAITON "GEDCOM" "MD5" md5)
			(struct REPRESENTAITON "GEDCOM" "text/plain" (slurp out-name))
			}
		      })]
    (with-open [rdr (reader out-name)]
      (reduce handler state  (gedcom-seq (line-seq rdr))))))

(defn to-csv [m]
  (for [entry m]
    [ (name  (key entry))
      (keys (first (val entry)))
      ]))


