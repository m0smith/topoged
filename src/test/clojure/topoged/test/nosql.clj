(ns topoged.test.nosql
  (:require [com.ashafa.clutch :as clutch])
  (:require [clojure.data.codec.base64 :as b64] [clojure.data.json :as json])
  (:require (com.ashafa.clutch
              [http-client :as http-client]
              [utils :as utils]
              
              [view-server :as view-server]))
  (:use [clojure.java.io :only [reader writer input-stream output-stream]]
	[clojure.contrib.seq-utils :only (find-first)]
	[clojure.contrib.io :only (to-byte-array)]
	[clojure.pprint :only (pprint)]
	[topoged.file :only (copy-md5)]
	[topoged.util :only (uuid)]
	[topoged.gedcom :only [gedcom-seq]]))


(def gedcom-file "src/test/resources/simple.ged")
(def initial-state {})
(def topoged-db "http://localhost:5984/topoged")
(def view-server-name "topoged")
(def view-name "type-views")

(def cache memoize)


(defn- write-json-java-util-uuid [x #^java.io.PrintWriter out escape-unicode?]
(.print out (json/json-str (str x))))

(extend java.util.UUID clojure.data.json/Write-JSON
    {:write-json write-json-java-util-uuid})


(def base-types [
     {
      :_id "369696f0-37e6-11e1-b86c-0800200c9a66"
      :friendly "SOURCE"
      }
     {
      :_id "369696f1-37e6-11e1-b86c-0800200c9a66"
      :friendly "PERSONA"
      }
     {
      :_id "369696f2-37e6-11e1-b86c-0800200c9a66"
      :friendly "GEDCOM"
      }
     {
      :_id "369696f3-37e6-11e1-b86c-0800200c9a66"
      :fiendly "NAME"
      }
     {
      :_id "369696f4-37e6-11e1-b86c-0800200c9a66"
      :friendly "REPRESENTAITON"
      }
     {
      :_id "369696f5-37e6-11e1-b86c-0800200c9a66"
      :friendly "MD5"
      }
     { :_id "369696f6-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696f7-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696f8-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696f9-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696fa-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696fb-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696fc-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696fd-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696fe-37e6-11e1-b86c-0800200c9a66" }
     { :_id "369696ff-37e6-11e1-b86c-0800200c9a66" }
     { :_id "36969700-37e6-11e1-b86c-0800200c9a66" }
     { :_id "36969701-37e6-11e1-b86c-0800200c9a66" }
     { :_id "36969702-37e6-11e1-b86c-0800200c9a66" }
     { :_id "36969703-37e6-11e1-b86c-0800200c9a66" }
     { :_id "36969704-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be00-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be01-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be02-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be03-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be04-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be05-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be06-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be07-37e6-11e1-b86c-0800200c9a66" }
     { :_id "3696be08-37e6-11e1-b86c-0800200c9a66" }
     ])

(defn encode-attachment [bytes]
  (String. (b64/encode bytes)))

(def friendly
  (cache
   (fn [friendlyid]
     (let [rtnval (clutch/get-view view-server-name view-name :friendly {:key friendlyid} )]
       (if (seq rtnval) (:id (first rtnval))
            (throw (IllegalArgumentException. (str "No such friendly:" friendlyid))))))))


(defn handler-factory [m]
  (fn [state rec]
    (if-let [func (get m (:tag rec))]
      (func state rec)
      state)))

(defn match-key [name value]
  (fn [m] (= value (get m name))))

(defn indi-handler [source]
  (fn [state rec]
    (let [PERSONA (friendly "PERSONA")
          handlers (handler-factory
                    { 
                     :NAME #(assoc % :name (:value %2))
                     :ADDR #(assoc % :addr (:value %2))
                    })]
      (clutch/with-db topoged-db
        (clutch/put-document (reduce handlers
                                     {:type PERSONA
                                      :source source
                                      :_id (:value rec)}
                                     (:content rec)))))
    state ))


(defn head-handler [source gedcom-file md5]
  (fn [state rec]
    (let [GEDCOM (friendly "GEDCOM")
          REPRESENTAITON (friendly "REPRESENTAITON")
          SOURCE (friendly "SOURCE")]
      (clutch/with-db topoged-db
        (clutch/put-document {:_id source
                              :type SOURCE
                              :source-type GEDCOM})
        (clutch/put-document {:_id (uuid)
                              :type REPRESENTAITON
                              :owner source
                              :md5 md5
                              :representation-type GEDCOM
                              :_attachments { gedcom-file {:content_type "text/gedcom"
                                                           :data (-> (slurp gedcom-file)
                                                                     to-byte-array
                                                                     encode-attachment)}}
                              })))
    state))

(defn load-types-single []
  (clutch/with-db topoged-db
    (doseq [type base-types]
      (println type)
      (clutch/put-document type))))

(defn load-types []
  (clutch/with-db topoged-db
    (clutch/bulk-update base-types)))

(defn init-db []
  (clutch/delete-database topoged-db)
  (clutch/create-database topoged-db)
  (load-types)
  ;;(clutch/configure-view-server view-server-name (view-server/view-server-exec-string)))
  (clutch/save-view
   view-server-name view-name
   (clutch/view-server-fns :clojure
			   {:friendly
			    {:map (fn [doc]
				    (when (:friendly doc)
				      [[(:friendly doc) (:_id doc)]]))}})))
  


(defn process-gedcom [f]
  (println (init-db))
  (let [out-name "/tmp/f.ged"
        source-zero (uuid)
        md5 (let [in  f
                  out out-name]
              (copy-md5 in out))
        state initial-state
        handler (handler-factory
                 {
                  :HEAD (head-handler source-zero out-name md5)
                  :INDI (indi-handler source-zero)
                  :SUBM (indi-handler source-zero)
                  })]
    (with-open [rdr (reader out-name)]
      (reduce handler state (gedcom-seq (line-seq rdr))))))



