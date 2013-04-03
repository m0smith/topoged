(ns topoged.type.wellknown)

(def type-document-id #uuid "faa130aa-810a-11e2-9e96-0800200c9a66")
(def representation-document-id #uuid "faa130ab-810a-11e2-9e96-0800200c9a66")
(def default-context-id #uuid "faa130b1-810a-11e2-9e96-0800200c9a66" )

(def well-known-types 
" [ key id type representation representationId ] "
[

;; BOOTSTRAPPED

 [ :type-document type-document-id "Indicates a record is a Type"  "Indicates a record is a Type" #uuid "42e61480-8114-11e2-9e96-0800200c9a66"]
 [ :representation-document representation-document-id "String"   "Indicates a record is a Representation" #uuid "894e95c0-8748-11e2-9e96-0800200c9a66"]
 [ :default-context default-context-id "String" "Default Context for a Representation" #uuid "894e95c1-8748-11e2-9e96-0800200c9a66"]

;; ATTRIBUTES
 [ :address #uuid "faa130bb-810a-11e2-9e96-0800200c9a66" "Address" "Address" #uuid "6a97b445-075e-40a9-8864-d6d615a1509e" ]
 [ :birth-date #uuid "faa130a1-810a-11e2-9e96-0800200c9a66" "Date of birth" "Date of birth" #uuid "e20423a2-8113-11e2-9e96-0800200c9a66"]
 [ :birth-place #uuid "faa130a2-810a-11e2-9e96-0800200c9a66" "Place of birth" "Place of birth" #uuid "e20423a3-8113-11e2-9e96-0800200c9a66"]
 [ :city #uuid "faa130b3-810a-11e2-9e96-0800200c9a66" "City" "City" #uuid "22ca5635-c8b7-4f32-8f7e-7d20a0e3f47f" ]
 [ :date #uuid "faa130bf-810a-11e2-9e96-0800200c9a66" "Date" "Date" #uuid "96a59209-8fef-4b8e-9cf8-38975bdb4b04"]
 [ :death-date #uuid "faa130a3-810a-11e2-9e96-0800200c9a66" "Date of death" "Date of death" #uuid "e20423a4-8113-11e2-9e96-0800200c9a66"]
 [ :death-place #uuid "faa130a4-810a-11e2-9e96-0800200c9a66" "Place of death" "Place of death"#uuid "e20423a5-8113-11e2-9e96-0800200c9a66" ] 
 [ :email #uuid "faa130bc-810a-11e2-9e96-0800200c9a66" "E-mail address" "E-mail" #uuid "018be481-c142-4000-9b03-016c8986f0be"]
 [ :female #uuid "faa130a9-810a-11e2-9e96-0800200c9a66" "Used by :gender for females" "Female" #uuid "e20423a9-8113-11e2-9e96-0800200c9a66"]
 [ :gender #uuid "faa130a5-810a-11e2-9e96-0800200c9a66" "The gender or sex of a person"   "Gender" #uuid "e20423a1-8113-11e2-9e96-0800200c9a66"] 
 [ :male #uuid "faa130a8-810a-11e2-9e96-0800200c9a66" "Used by :gender for males" "Male" #uuid "e20423a8-8113-11e2-9e96-0800200c9a66"]
 [ :marriage-date #uuid "faa130a6-810a-11e2-9e96-0800200c9a66" "Date of marriage" "Date of marriage" #uuid "e20423a6-8113-11e2-9e96-0800200c9a66"]
 [ :marriage-place #uuid "faa130a7-810a-11e2-9e96-0800200c9a66" "Place of marriage" "Place of marraige" #uuid "e20423a7-8113-11e2-9e96-0800200c9a66"]
 [ :name #uuid "faa130a0-810a-11e2-9e96-0800200c9a66" "The name of some item, used as the display name"   "Name" #uuid "e20423a0-8113-11e2-9e96-0800200c9a66"]
 [ :given-name #uuid "faa157c0-810a-11e2-9e96-0800200c9a66" "Given or christian name" "Given Name" #uuid "aab16f3e-bb66-46cd-819f-6794c8dbc8a5"]
 [ :surname #uuid "faa157c1-810a-11e2-9e96-0800200c9a66" "Surname or family name" "Surname"#uuid "4a1a711d-2da0-47ac-934d-bff9af6b2288"]
;; 
;; 
 [ :notes #uuid "faa130be-810a-11e2-9e96-0800200c9a66" "Notes" "Notes" #uuid "f7b29d12-a689-4538-81db-81091975e73b" ]
 [ :phone #uuid "faa130bd-810a-11e2-9e96-0800200c9a66" "Phone Number" "Phone" #uuid "73b3f3c9-3b7f-4668-92a0-6eb540d18f4e" ]

;; LDS


 [:lds-baptism #uuid "faa157b6-810a-11e2-9e96-0800200c9a66" "LDS Baptism" "Baptised" #uuid "66baf6bf-1143-41c5-9dc2-eea506d7f6e6"]
 [:lds-endowment #uuid "faa157b7-810a-11e2-9e96-0800200c9a66" "LDS Endowment" "Endowed" #uuid "d6511d3b-d731-4571-940c-8796e6ed2df5"]
 [:lds-sealed-to-parents #uuid "faa157b8-810a-11e2-9e96-0800200c9a66" "LDS Sealed to parents" "Sealed to Parents" #uuid "3c89e5d7-3d61-4a84-bf1c-f3be91b8859c"]
 [:lds-sealed-to-spouse #uuid "faa157b9-810a-11e2-9e96-0800200c9a66" "LDS Sealed to spouse" "Sealed to Spouse" #uuid "951faf30-ac57-42a3-ac64-f3c6f5fbfe77"]
 [:temple #uuid "faa157bb-810a-11e2-9e96-0800200c9a66" "LDS Temple" "Temple" #uuid "462eb734-4562-48d7-82e7-23475d8a8559"]

;; DOCTYPES

 [ :persona-document #uuid "faa130ac-810a-11e2-9e96-0800200c9a66" "Indicates a record is a Persona" 
  "Indicates a record is a Persona" #uuid "4ae5e460-8116-11e2-9e96-0800200c9a66"]
 [ :source-document #uuid "faa130ad-810a-11e2-9e96-0800200c9a66" "Indicates a record is a Source" "Indicates a record is a Source" #uuid "4ae5e461-8116-11e2-9e96-0800200c9a66"]
 [ :individual-document #uuid "faa130ae-810a-11e2-9e96-0800200c9a66" "Indicates a record is a Individual" "Indicates a record is a Individual" #uuid "4ae5e462-8116-11e2-9e96-0800200c9a66"]
 [ :event-group-document #uuid "faa130af-810a-11e2-9e96-0800200c9a66" "Indicates a record is a Group" "Indicates a record is a Group" #uuid "4ae5e463-8116-11e2-9e96-0800200c9a66"]
 [ :attachment-document  #uuid "faa130c2-810a-11e2-9e96-0800200c9a66"  "Indicates a record is an Attachment" "Indicates a record is an Attachment" #uuid "f46b7b3b-c2c2-4bb2-a01e-a08592f76979"]
 [ :attribute-document #uuid "faa130c9-810a-11e2-9e96-0800200c9a66" "Indicates a record is an Attribute of another record" "Attribute" #uuid "de14663a-4803-42af-849b-19a47798a40e"]

;; SOURCE Citation

 [ :accessedDate #uuid "faa130b4-810a-11e2-9e96-0800200c9a66" "Date a source was accessed, dowloaded, etc." "Accessed Date" #uuid "5b244a94-e8a7-4756-9d58-1608a4768d39" ]
 [ :author #uuid "faa130ba-810a-11e2-9e96-0800200c9a66" "The author, composer, created of a work" "Author "#uuid "aa1ed313-b8dd-45aa-be57-d7964d475285"]
 [ :citationType #uuid "faa130b7-810a-11e2-9e96-0800200c9a66" "What type of citation is this: Book, Article, GEDCOM, WEb Page, etc." "Citation Type" #uuid "cd1d0f0d-211a-4beb-8191-3747e6dda832" ]
 [ :createdDate #uuid "faa130b5-810a-11e2-9e96-0800200c9a66" "Date a source was created, etc." "Created Date" #uuid "bba149d7-1520-4bf3-8c0d-dcd5fdd86a94" ]
 [ :mediaType #uuid "faa130b8-810a-11e2-9e96-0800200c9a66" "Photo, Map, Chart, Graphic, GEDCOM, etc" "Media" #uuid "45fd233b-6406-4777-8719-07d7abdca314"]
 [ :mediumTypeId #uuid "faa130b6-810a-11e2-9e96-0800200c9a66" "What medium is soruce published in WEB, paper, DVD, CD-ROM, etc" "Medium" #uuid "2aabf12b-d290-412c-90e2-e7025ad188aa"]
 [ :publisher #uuid "faa130c0-810a-11e2-9e96-0800200c9a66" "Publisher" "Publisher" #uuid "ead8de70-416f-4521-ae1d-9087c7453078"]
 [ :title #uuid "faa130c1-810a-11e2-9e96-0800200c9a66" "Title of a work source" "Title" #uuid "88466804-8b60-4725-8de3-a0b7efd1e5ae" ]
 [ :characterSet #uuid "faa130c3-810a-11e2-9e96-0800200c9a66" "The character set of the source" "Charset" #uuid "dae16136-d986-4698-bf06-b5837c3c94c1"]
 [:version  #uuid "faa130c4-810a-11e2-9e96-0800200c9a66" "A version" "Version" #uuid "da471d84-fec7-4fc5-8c74-bb8c5d926ca9"]
 [:form #uuid "faa130c5-810a-11e2-9e96-0800200c9a66" "Form" "Form"  #uuid "d0744cf2-13b4-42cb-a4d8-82d76fb1a326" ]
 [ :corporation #uuid "faa130c6-810a-11e2-9e96-0800200c9a66" "Commerical Entity" "Corporation" #uuid "d10ebf8a-d683-4524-9833-24ca2915ed6e" ]
 [ :destination #uuid "faa130c7-810a-11e2-9e96-0800200c9a66" "Destination" "Destination" #uuid "e408a986-93f3-4ce6-a3b7-b4db8e94af25"]
 [ :copyright #uuid "faa130c8-810a-11e2-9e96-0800200c9a66" "Copyright of a work" "Copyright" #uuid "de80bd10-d894-4442-9dab-78e73f350ff3" ]
 [ :ancestral-file-number #uuid "faa130cb-810a-11e2-9e96-0800200c9a66" "Ancestral File Number" "AFN" #uuid "d520067e-ff9f-4dc3-8c30-2251de8709b6"]
 
 ;; CITATION TYPES
 
 [ :web-based-media #uuid "faa130b2-810a-11e2-9e96-0800200c9a66" "A Citation type indicating a source that exists on the web" "Web Based" #uuid "9f2dec9a-f71e-4ea2-8885-ec5a1cf11508"]
 
 ;; MEDIUM TYPES
 
 [:web-medium #uuid "faa130b9-810a-11e2-9e96-0800200c9a66" "Web-based medium" "Web" #uuid "5a6be71e-e46c-454b-87e2-0aca7049af38"]
 
 ;; REPOSITORY
 
 ;; PERSONA
 
 [ :id-in-source #uuid "faa130ca-810a-11e2-9e96-0800200c9a66" "ID of a document in its source" "ID in Source" #uuid "b4ce958b-74fe-4911-8594-c570459c3f05"]
 
 ;; GROUP/EVENT
 
 [:child #uuid "faa157b1-810a-11e2-9e96-0800200c9a66" "Child in a family" "Child" #uuid "7758d93e-c6ca-4c8e-b89d-f75fbe4215ee"] 
 [:baptismal-candidate #uuid "faa157b2-810a-11e2-9e96-0800200c9a66" "Person being baptized" "Candidate" #uuid "4bf3f4e7-8c11-46c3-8d84-4c4249a5973c"]
 [:deceased #uuid "faa157b3-810a-11e2-9e96-0800200c9a66" "Person who passed away" "Deceased" #uuid "8d21fd98-0880-43b8-bf13-bb3bc84f753e"]
 [:recipient #uuid "faa157ba-810a-11e2-9e96-0800200c9a66"  "A person who has received a gift, endownment, etc" "Recipient" #uuid "88c3b4f9-3ec7-4fa5-8ca5-f9060123b787"]
 [:parents #uuid "faa157b4-810a-11e2-9e96-0800200c9a66" "The parents in a lineage linked group" "Parents" #uuid "a6273ec5-d045-4783-aac3-30a742ce1828"]
 [:children #uuid "faa157b5-810a-11e2-9e96-0800200c9a66" "The children in a lineage linked group" "Children" #uuid "d32eb7d2-2af1-4b87-b5a2-dd5bf5ccb28c"]
 [ :birth #uuid "faa130cc-810a-11e2-9e96-0800200c9a66" "Birth" "Birth" #uuid "4b07e170-88f6-42df-9cc5-18df85fe018d"]
 [ :place #uuid "faa157b0-810a-11e2-9e96-0800200c9a66" "Place" "Place" #uuid "fa069171-0e3b-4966-897a-13ab86df9317"]
 [:marriage #uuid "faa157bc-810a-11e2-9e96-0800200c9a66" "Marriage" "Marriage" #uuid "a6dd5b03-3641-41ba-8bfd-92932a954260"]
 [:christening #uuid "faa157bd-810a-11e2-9e96-0800200c9a66" "Chrisening" "Christening" #uuid "7e5f707a-5e5a-45c5-8b05-3ea4959380d9"]
 [:death #uuid "faa157be-810a-11e2-9e96-0800200c9a66" "Death" "Death" #uuid "3601cf08-7e2f-4ff5-94a8-f823830ab20e"]
 [:burial #uuid "faa157bf-810a-11e2-9e96-0800200c9a66" "Burial" "Burial" #uuid "de74f6dd-f27e-4b96-b2cf-ce803a1e6b7f"]


]) 


;; From: http://www.famkruithof.net/uuid/uuidgen
;;  A list of UUIDs to use as future well-known types
;; 
;; 
;; 

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

(defn doc-line [[key id desc rep]]
  (str \tab "<tr><td>" key "</td><td>" id "</td><td>" desc "</td><td>" rep "</td><tr>" \newline))

(defn doc-page []
  (str "This page is generated by calling topoged.type.wellknown/doc-page."
       "Do not edit this page manually" \newline \newline
       "<table>" \newline
       \tab "<tr><th>Key</th><th>UUID</th><th>Description</th><th>Representation</th></tr>" \newline
       (apply str (sort (map doc-line well-known-types)))
       "</table>" \newline \newline))
