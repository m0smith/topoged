(ns topoged.gedoverse)
(set! *warn-on-reflection* true)

(def logger (agent (list)))

(defn log [msg]
  (do
    (send logger #(cons %2 %1) msg)
    msg))


(defn add [map element]
  (assoc map (:id element) element))

(defstruct gedoverse-struct :source :group :persona)

(def gedoverse-agent (agent (struct gedoverse-struct {} {} {})))




