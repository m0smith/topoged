(ns topoged.gedoverse)
(set! *warn-on-reflection* true)

(def logger (agent (list)))

(defn log [msg]
  (do
    (send logger #(cons %2 %1) msg)
    msg))

(let [source-map (agent {})]
  (defn sources [] @source-map)
  (defn add-source [s]
    (letfn [(cause [sm]
		  (log sm)
		  (assoc sm (:id s) s))]
      (fn [] (send source-map cause)))))

(let [group-map (agent {})]
  (defn groups [] @group-map)
  (defn add-group [s]
    (letfn [(cause [sm]
		  (log sm)
		  (assoc sm (:id s) s))]
      (fn [] (send group-map cause)))))

(let [persona-map (agent {})]
  (defn personas [] @persona-map)
  (defn add-persona [p]
    (letfn [(cause [pm]
		  (log pm)
		  (assoc pm (:id p) p))]
      (fn [] (send persona-map cause))))
  (defn persona-cause [f]
    (fn [] (send persona-map #(do (f) %)))))

