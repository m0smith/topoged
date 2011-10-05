(ns db
  (:require [topoged.util :as util])
  (:require [topoged.hibernate :as hib]))

(hib/init #(do (println "init")
	   (hib/hibernate-properties-config "src/test/resources/hibernate.properties"
					    "src/test/resources/*.hbm.xml")))

(defn id-factory "Creates a 16 element byte array representation of a uuid"
  []
  (let [id (util/uuid)
	msb (.getMostSignificantBits id)
	lsb (.getLeastSignificantBits id)]
    (byte-array (mapcat util/long-to-bytes [msb lsb]))))


(defmacro add-entity-factory-auto-id
  "A macro that creates a function that will add a record of the given entity"
  [name factory & columns]
  (let [id (gensym "id")]
    `(fn [~@columns] 
       (let [~id (~factory)
	     entity-name# ~name
	     data# ~(into {"id" id} (map (fn [f] {(str f) f}) columns))]
	 (hib/with-hibernate-tx [session# tx#]
	   (.save session# entity-name# (java.util.HashMap. data#)))))))

(defmacro add-entity-factory
  "A macro that creates a function that will add a record of the given entity"
  [name & columns] 
  `(fn [~@columns] 
     (let [entity-name# ~name
	   data# ~(into {} (map (fn [f] {(str f) f}) columns))]
       (hib/with-hibernate-tx [session# tx#]
	 (.save session# entity-name# (java.util.HashMap. data#))))))

(def add-type (add-entity-factory-auto-id "Type" id-factory name desc))

(def add-type-group (add-entity-factory-auto-id "TypeGroup" id-factory group_type rel_type  type_group_members))

(defn fetch-by-id [table id]
  (hib/with-hibernate-tx [session tx]
    (.load session table id)))

(defn list-from [table]
  (hib/with-hibernate-tx [session _]
    (.. session (createQuery (str "from " table)) list)))

(defn list-types [] (list-from "Type"))

(def types '({"id" (39 -34 -67 44 73 102 78 -81 -78 46 -101 70 84 -74 -5 106),
	     "desc" "Relationship type for a simple grouping", "name" "SIMPLE", "$type$" "Type"}
	    {"id" (-9 24 -36 -92 13 4 69 -64 -93 1 102 -55 -23 73 -120 60),
	     "desc" "Mother Role in a family", "name" "MOTHER", "$type$" "Type"}
	    {"id" (-108 120 67 124 -10 10 67 -73 -80 4 -8 -41 -51 31 -17 -102),
	     "desc" "Father Role in a family", "name" "FATHER", "$type$" "Type"}
	    {"id" (23 66 102 74 10 107 78 -53 -109 77 -126 -58 75 -19 111 114),
	     "desc" "Child Role in a family", "name" "CHILD", "$type$" "Type"}
	    {"id" (56 -124 -110 -19 -37 -64 64 17 -102 -112 70 3 -11 -33 -68 26),
	     "desc" "Mother Role in a family", "name" "MOTHER", "$type$" "Type"}
	    {"id" (105 67 14 61 -57 47 66 56 -96 37 95 -121 -91 -85 22 97),
	     "desc" "Family Group", "name" "FAMILY", "$type$" "Type"}))

(defn save-types []
  (hib/with-hibernate-tx [session tx]
    (doseq [type types]
      (let  [ outtype (java.util.HashMap.
		       (update-in type [ "id" ] #( byte-array (map byte %)))) ] 
	( .save session "Type" outtype)))))

(defn dump-types [] (map (fn [kk] (update-in (into {} kk) [ "id" ] #(seq %))) (list-types)))

(defn find-type-id [s] (get (first (filter #(= s (get %"name")) (list-types))) "id"))

;;(add-type-group (find-type-id "FAMILY") (find-type-id "SIMPLE") #{ (fetch-by-id "Type" (find-type-id "FATHER") ) (fetch-by-id "Type" (find-type-id "CHILD") ) })