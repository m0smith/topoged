(ns topoged.test.entity
  (:use [clojure.java.io :only [reader writer input-stream output-stream]]
	[clojure.contrib.seq-utils :only (find-first)]
	[clojure.pprint :only (pprint)]
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
(defstruct SOURCE_WITHIN_SOURCE :PARENT_SOURCE_ID :MEMBER_SOURCE_ID :ORDER)
(defstruct REPRESENTAITON :SOURCE_ID :TYPE_ID :CONTENT :COMMENTS)
(defstruct REPOSITORY :REPOSITORY_ID :TYPE_ID )
(defstruct REPOSITORY_SOURCE :REPOSITORY_ID :SOURCE_ID :CALL_NUMBER :DESCRIPTION)
(defstruct REPOSITORY_ATTRIBUTE :REPOSITORY_ID :ATTRIBUTE_ID)

(defstruct PERSONA :PERSONA_ID)
(defstruct PERSONA_SOURCE :PERSONA_ID :SOURCE_ID)
(defstruct PERSONA_ATTRIBUTE :PERSONA_ID :ATTRIBUTE_ID)
(defstruct ATTRIBUTE_ATTRIBUTE :PARENT_ATTRIBUTE_ID :MEMBER_ATTRIBUTE_ID)
(defstruct ATTRIBUTE :ATTRIBUTE_ID :TYPE_ID :VALUE)


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
	       (struct TYPE "text/plain" "text/plain")
	       }

      })

(defn handler-factory [m]
  (fn [state rec]
    (if-let [func (get m (:tag rec))]
      (func state rec)
      state)))

(defn match-in-fn [ [& ks] val]
  #(= val (get-in % ks)))


(defn subm-handler [source-zero]
  (fn [state rec]
    (let [repo-id (:value rec)
	  state (-> state
		    (update-state  :repository
				   (struct REPOSITORY repo-id "HOME"))
		    (update-state  :repository_source
				   (struct REPOSITORY_SOURCE repo-id (:SOURCE_ID source-zero))))]
      (reduce #(let [line-number (get-in %2 [:attrs :line-number])
		     rep (get-in %2 [:attrs :representation]) 
		     tag (:tag %2)
		     source-id (str "SOURCE" line-number)] 
		 (-> %1
		     (update-state :source
				   (struct SOURCE
					   source-id
					   "LINE"))
		     (update-state :source_within_source
				   (struct SOURCE_WITHIN_SOURCE
					   (:SOURCE_ID source-zero)
					   source-id
					   line-number))
		     (update-state :representation
				   (struct REPRESENTAITON
					   source-id
					   "text/plain"
					   rep))
		     (update-state :attribute
				   (struct ATTRIBUTE
					   (str "ATTR" line-number)
					   (name tag)
					   (:value %2)
					   ))
		     (update-state :type
				   (struct TYPE (name tag) (name tag)))
		     (update-state :repository_attribute
				   (struct REPOSITORY_ATTRIBUTE repo-id
					   (str "ATTR" line-number)))))
	      
	      state (:content rec)))))

(defn indi-handler [source-zero]
  (fn [state rec]
    (let [persona-id (:value rec)
	  state (-> state
		    (update-state  :persona
				   (struct PERSONA persona-id ))
		    (update-state  :repository_source
				   (struct PERSONA_SOURCE persona-id (:SOURCE_ID source-zero))))]
      (letfn [(persona-attribute [state line-number]
				 (update-state state :persona_attribute
					       (struct PERSONA_ATTRIBUTE persona-id
						       (str "ATTR" line-number))))
	      (attr-attribute [attr-id]
			      (fn [state line-number]
				(update-state state :attribute_attribute
					      (struct ATTRIBUTE_ATTRIBUTE attr-id
						      (str "ATTR" line-number)))))
	      (attr-attribute-x [attr-id]
			      (fn [state line-number]
				(prn line-number)
				state))
	      (content-handler [attr-parent-func]
			       (fn [state rec]
				 (let [line-number (get-in rec [:attrs :line-number])
				       rep (get-in rec [:attrs :representation]) 
				       tag (:tag rec)
				       source-id (str "SOURCE" line-number)] 
				   (-> state
				       (update-state :source
						     (struct SOURCE
							     source-id
							     "LINE"))
				       (update-state :source_within_source
						     (struct SOURCE_WITHIN_SOURCE
							     (:SOURCE_ID source-zero)
							     source-id
							     line-number))
				       (update-state :representation
						     (struct REPRESENTAITON
							     source-id
							     "text/plain"
							     rep))
				       (update-state :attribute
						     (struct ATTRIBUTE
							     (str "ATTR" line-number)
							     (name tag)
							     (:value rec)
							     ))
				       (update-state :type
						     (struct TYPE (name tag) (name tag)))
				       (attr-parent-func line-number)
				       (reduce-content rec (attr-attribute  (str "ATTR" line-number)))
				       ))))
	      (reduce-content [state rec func]
			      (let [content (:content rec)]
				(if (seq content)
				  (let [ch (content-handler func)]
				    (reduce ch state content))
				  state)))]
	(reduce-content state rec persona-attribute)))))

(defn process-gedcom [f]
  (let [out-name "/tmp/f.ged"
	source-zero (struct SOURCE "SOURCE0" "GEDCOM")
	md5 (let [in  f
		  out out-name]
	      (copy-md5 in out))
	state (merge initial-state
		     { :source,
		      #{
			source-zero
			}
		      :representation
		      #{
			(struct REPRESENTAITON "GEDCOM" "MD5" md5)
			(struct REPRESENTAITON "GEDCOM" "text/plain" (slurp out-name))
			}
		      })
	handler (handler-factory
		 {
		  :HEAD #(merge %1 {:forward
				    {
				     (-> (find-first (match-in-fn [:tag] :SUBM) (:content %2) )
					 :value)
				     source-zero}})
		  :SUBM (subm-handler source-zero)
		  :INDI (indi-handler source-zero)
		  })]
    (with-open [rdr (reader out-name)]
      (reduce handler state  (gedcom-seq (line-seq rdr))))))

(defn to-csv [m]
  (for [entry m]
    [ (name  (key entry))
      (keys (first (val entry)))
      ]))


