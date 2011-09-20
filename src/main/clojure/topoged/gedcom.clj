(ns topoged.gedcom
  (:use [ clojure.contrib.duck-streams :only (read-lines)])
  (:use [ clojure.contrib.string :only (split)])
  (:use clojure.pprint)
  (:require [topoged.util :as util]))


(set! *warn-on-reflection* true)

(defn gedcom?
  "Return true if the string resembles a gedcom file by looking for '0 HEAD' as the first 6 charaters"
  [f]  (if (and (string? f) (> (count f) 6))
         (re-matches #"0 [A-Z][A-Z][A-Z][A-Z]" (subs f 0 6)))
  )

(defn input-seq
  "Convert the argument into a sequecnce of lines."
  [f]
  (if (gedcom? f)
    (filter #(> (count %) 0) (re-seq #".*" f))
    (read-lines f))
    
  )


(defn level? [level] (fn [coll] (= level (first coll))))

(defn gedcom-reduce-content
  "Reduce a partition with records from a single stanza to an xml-like structure by adding
the sub-stanzas to the :content of the record"
  ([prt]
     (let [[level rec] (first prt)]
       (assoc rec :content (gedcom-reduce-content (rest  prt) []))))
  ([prt vec]
     (if-let [prt (seq prt)]
       (let [[level rec] (first prt)
             subparts (util/partition-starting-every (level? level) prt)]
         (reduce conj [] (map gedcom-reduce-content subparts))))))

(defrecord GedcomAttrributes [level line-number representation])
(defrecord GedcomRecord [tag value attrs content])

(defn gedcom-line-to-vector
  "Convert a line from a gedcom file into a vector with level and a map"
  [lineno line]
  (let [[level preid prerest]  (split #" " 3 line)
        [id value] (if (re-matches #"@.*@" preid) [prerest preid] [preid prerest])
        attrmap {:level level :tag id}
	attrs (GedcomAttrributes. level lineno line) ]
    [level (GedcomRecord. (keyword id) value attrs nil)]))

(defn source-line "Get a vector with [level GedcomRecord] and return the source line"
  [[level rec]]  (-> rec :attrs :representation))

(defn gedcom-partitions  [f]
  "Partition the gedcom lines into stanzas"
  (let [inseq (input-seq f)
        lineseq (map-indexed #(gedcom-line-to-vector (inc %1) %2) inseq)
        parts (util/partition-starting-every (level? "0") lineseq)]
    parts ))

 (defn gedcom-seq
  "Return a sequence of GEDCOM records following the pattern that the XML parsing uses."
  [f] (map #(assoc (gedcom-reduce-content %) 
              :source-stanza-representation (reduce str (interpose \newline (map source-line %))))
           (gedcom-partitions f)))


(defn parse
  "Parse a GEDCOM file and produce an structure similar to the xml parse."
  [f] { :tag :GEDCOM :content (gedcom-seq f)} )


(defn INDI [f]
  (let [c (-> f :content)
	name  (map #(-> % :attrs :value) (filter #(= (% :tag) :NAME) c))]
    name))

;;(take 3 (map #(apply-symbol (:tag %) (list %)) gseq))

;;
;; test stuff
;;
(comment
  (def file "src/test/resources/simple.ged")

  (def parts (gedcom-partitions file))

  (def xml (parse file)))


