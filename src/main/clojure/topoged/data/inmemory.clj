(ns topoged.data.inmemory
  (:use [topoged.util]))

(def db (atom []))

(def persona-keys [:sourceId ] )
(def persona-type :PERSONA)

(def source-keys [] )
(def source-type :SOURCE)

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




(defn add-entity [req-keys type args ]
  (let [m (validate-args req-keys args)
        m (assoc m :type type :id (uuid))]
    (swap! db conj m)
    m))

(defn add-persona [  & args ]
  (add-entity persona-keys persona-type args))

(defn add-source [ & args ]
  (add-entity source-keys source-type args))

(defn entity-query-pred [type pred & kys]
  (for [ent @db :when (and (#{type} (:type ent)) (pred ent))]
    (mapv ent kys)))

(defn entity-query [type & kys]
  (for [ent @db :when (#{type} (:type ent))]
    (mapv ent kys)))

(defn persona-names []
  (entity-query persona-type :id :name))

       

