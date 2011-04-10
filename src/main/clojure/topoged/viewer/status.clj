(ns topoged.viewer.status)

(def status-active (agent {}))
(def status-completed (agent {}))

(defstruct status-element :uuid :data :funcs :state)

(defn status-begin [uuid tab-init-func popup-init-func initial-data]
  (let [tab tab-init-func
	popup popup-init-func
	element (struct status-element uuid initial-data [tab popup] :begin)]
  (send status-active
	   (fn [sa] (assoc sa uuid element))))))

(defn status-update [uuid data]
  (send status-active
	(fn [sa]
	  (let [ element (sa uuid)]
	    (assoc sa uuid
			(assoc element :data data :state :active))))))

(defn status-complete [uuid]
  (let [stat (assoc (@status-active uuid) :state :completed)]
    (send status-active
	(fn [sa]
	  (dissoc sa uid)))
    (send status-completed (assoc uuid stat))))

(defn formatted-status-begin [uuid pattern init-data]
  (status-begin uuid
		(fn [parent]
		  )
		(fn [_] (fn [_])) init-data))
  

