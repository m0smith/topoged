(ns topoged.data.common)

(def UNDEFINEDX #uuid "a4d6c4d6-bb29-45ca-8bf6-25c06168a8d5")
(def UNDEFINED nil)

(def persona-keys [:sourceId ] )
(def persona-type :PERSONA)

(def source-keys [] )
(def source-type :SOURCE)
(def source-type-gedcom #uuid "feb55486-ac9e-4ec1-a03e-0fece2c29eb8") 


(def lineage-keys [:sourceId :parents :children] )
(def lineage-type :GROUP)
(def lineage-group-type :LINEAGE)

(defn illegal-args [msg]
  (throw (IllegalArgumentException. msg)))

(defn- args-to-map [ [f & rest :as args] ]
  (cond
   (map? f) f
   (even? (count args)) (apply hash-map args)
   :else (illegal-args "Either a map or key value pairs")))

(defn- has-keys [m kys]
  (every? m kys))

(defn validate-args [ kys args ]
  (let [m (args-to-map args)]
    (if (has-keys m kys)
      m
      (illegal-args (str "Missing required keys: " kys)))))


(defn mmap [f coll]
  (map #(map f %) coll))

(defn ffilter [flt pred coll]
  (filter #(flt pred %) coll))

(defn mapvkeys [kys coll]
  (map #(mapv % kys) coll))

(defn ffilterp [pred coll]
  (ffilter some pred coll))

(defn filter-get [k pred coll]
  (ffilter pred #(get % k) coll))
