(ns topoged.type.wellknown)

(def well-known-types
[
 [ :name #uuid "faa130a0-810a-11e2-9e96-0800200c9a66" "String" "Name"]
 [ :gender #uuid "faa130a5-810a-11e2-9e96-0800200c9a66" "String" "Gender"] 
 [ :birth-date #uuid "faa130a1-810a-11e2-9e96-0800200c9a66" "String" "Date of birth"]
 [ :birth-place #uuid "faa130a2-810a-11e2-9e96-0800200c9a66" "String" "Place of birth"]
 [ :death-date #uuid "faa130a3-810a-11e2-9e96-0800200c9a66" "String" "Date of death"]
 [ :death-place #uuid "faa130a4-810a-11e2-9e96-0800200c9a66" "String" "Place of death" ] 
 [ :marriage-date #uuid "faa130a6-810a-11e2-9e96-0800200c9a66" "String" "Date of marriage"]
 [ :marriage-place #uuid "faa130a7-810a-11e2-9e96-0800200c9a66" "String" "Place of marraige"]
 [ :male #uuid "faa130a8-810a-11e2-9e96-0800200c9a66" "String" "Male"]
 [ :female #uuid "faa130a9-810a-11e2-9e96-0800200c9a66" "String" "Female"]

 [ :type-document #uuid "faa130aa-810a-11e2-9e96-0800200c9a66" "String" "Indicates a record is a Type"]
 [ :representation-document #uuid "faa130ab-810a-11e2-9e96-0800200c9a66" "String" "Indicates a record is a Representation"]
 [ :persona-document #uuid "faa130ac-810a-11e2-9e96-0800200c9a66" "String" "Indicates a record is a Persona"]
 [ :source-document #uuid "faa130ad-810a-11e2-9e96-0800200c9a66" "String" "Indicates a record is a Source"]
 [ :individual-document #uuid "faa130ae-810a-11e2-9e96-0800200c9a66" "String" "Indicates a record is a Individual"]
 [ :group-document #uuid "faa130af-810a-11e2-9e96-0800200c9a66" "String" "Indicates a record is a Group"]

]) 

(def representation-id
  {:name #uuid "e20423a0-8113-11e2-9e96-0800200c9a66"
   :gender #uuid "e20423a1-8113-11e2-9e96-0800200c9a66"
   :birth-date #uuid "e20423a2-8113-11e2-9e96-0800200c9a66"
   :birth-place #uuid "e20423a3-8113-11e2-9e96-0800200c9a66"
   :death-date #uuid "e20423a4-8113-11e2-9e96-0800200c9a66"
   :death-place #uuid "e20423a5-8113-11e2-9e96-0800200c9a66"
   :marriage-date #uuid "e20423a6-8113-11e2-9e96-0800200c9a66"
   :marriage-place #uuid "e20423a7-8113-11e2-9e96-0800200c9a66"
   :male #uuid "e20423a8-8113-11e2-9e96-0800200c9a66"
   :female #uuid "e20423a9-8113-11e2-9e96-0800200c9a66"
   :type-document #uuid "42e61480-8114-11e2-9e96-0800200c9a66"
   :representation-document #uuid "42e61481-8114-11e2-9e96-0800200c9a66"
   :persona-document #uuid "4ae5e460-8116-11e2-9e96-0800200c9a66"
   :individual-document #uuid "4ae5e461-8116-11e2-9e96-0800200c9a66"
   :source-document #uuid "4ae5e462-8116-11e2-9e96-0800200c9a66"
   :group-document #uuid "4ae5e463-8116-11e2-9e96-0800200c9a66"})
  


;; From: http://www.famkruithof.net/uuid/uuidgen
;;  A list of UUIDs to use as future well-known types
;; 
;; 
;; 
;; 
;; 
;; 
;; 
;; #uuid "faa130b0-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b1-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b2-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b3-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b4-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b5-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b6-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b7-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b8-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130b9-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130ba-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130bb-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130bc-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130bd-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130be-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130bf-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c0-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c1-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c2-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c3-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c4-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c5-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c6-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c7-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c8-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130c9-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130ca-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130cb-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa130cc-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b0-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b1-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b2-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b3-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b4-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b5-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b6-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b7-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b8-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157b9-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157ba-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157bb-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157bc-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157bd-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157be-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157bf-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c0-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c1-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c2-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c3-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c4-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c5-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c6-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c7-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c8-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157c9-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157ca-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157cb-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157cc-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157cd-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157ce-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157cf-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d0-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d1-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d2-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d3-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d4-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d5-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d6-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d7-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d8-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157d9-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157da-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157db-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157dc-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157dd-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157de-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157df-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157e0-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157e1-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157e2-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157e3-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157e4-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157e5-810a-11e2-9e96-0800200c9a66"
;; #uuid "faa157e6-810a-11e2-9e96-0800200c9a66"


(def well-known-type-by-key 
  (reduce #(assoc %1 (first %2) (second %2)) {} well-known-types))

(def well-known-type-by-id
  (reduce #(assoc %1 (second %2) (first %2)) {} well-known-types))

(defn doc-line [[key id cls desc]]
  (str \tab "<tr><td>" key "</td><td>" id "</td><td>" cls "</td><td>" desc "</td><tr>" \newline))

(defn doc-page []
  (str "This page is generated by calling topoged.type.wellknown/doc-page."
       "Do not edit this page manually" \newline \newline
       "<table>" \newline
       \tab "<tr><th>Key</th><th>UUID</th><th>Class</th><th>Representation</th></tr>" \newline
       (apply str (sort (map doc-line well-known-types)))
       "</table>" \newline \newline))
