(ns topoged.init
  (:import [com.tinkerpop.blueprints.impls.tg TinkerEdge TinkerVertex])
  (:require[archimedes.core :as g]
           [archimedes.vertex :as v]
           [archimedes.edge :as e]
           [topoged.db :as tdb]))

(defrecord TopogedContext [user db locale])

(def tower-config
  {
   :fallback-locale :en
   :dictionary
   {:en  {:pedigree {:Pedigree "Pedigree"
                     :Tree "Tree"
                     :Descendants "Descendents"
                     :Fractal "Fractal"}
          :menu    {:File "File"
                    :Settings "Optiones"
                    :SettingsTip "Settings for TOPOGED."
                    :Reports "Reports"
                    :Tasks "Tasks"
                    :ImportGedcom "Import GEDCOM"
                    :ImportGedcomTip "Import a GEDCOM file."}}
    :es  {:pedigree {:Pedigree "Cuadro geneal\u00f3gico"
                     ;:tree     "árbol"
                     :Tree     "\u00c1rbol"
                     :Descendants "Descendencia"
                     :Fractal "Fractal"}
          :menu     {:File "Archivo"
                     :Settings "Optiones"
                     :SettingsTip "Optiones de TOPOGED."
                     :Reports "Reportes"
                     :Tasks "Tareas"
                     :ImportGedcom "Importa GEDCOM"
                     :ImportGedcomTip "Importa un archivo de GEDCOM."}}}})

(defn- create-context []
  (g/use-clean-graph!)
  (let [db (reify tdb/DataStore
             (tdb/add-node [_ data-map] (v/create! data-map))
             
             (tdb/add-edge [_ start label end data-map] 
               (e/connect-with-id! nil start label end data-map))
             (tdb/find-by-kv [_ ky vl] (v/find-by-kv ky vl)))]
    (extend-type TinkerVertex
      tdb/DataStoreNode
      (tdb/merge-node [node data-map] (v/merge! node data-map))
      (tdb/to-data-map [node] (v/to-map node)))


    
    (extend-type TinkerEdge
      tdb/DataStoreNode
      (tdb/merge-node [node data-map] (v/merge! node data-map))
      (tdb/to-data-map [node] (v/to-map node)))
                           
    (->TopogedContext (tdb/add-node db {:type :researcher}) db :es)))

(defn topoged-init []
  (create-context)
  ;(db/init)
  ;(type-init)
  )

  
