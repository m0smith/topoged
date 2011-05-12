(ns topoged.viewer.status)

(defstruct status-agent-struct :active :completed)

(def status-agent (agent (struct status-agent-struct {} (list))))

(defstruct status-element :uuid :data :funcs :state)

(defn status-begin [uuid status-bar tab-init-func popup-init-func initial-data]
  (let [panel (javax.swing.JPanel.)]
    (doto status-bar (.add panel))
    (let [tab (tab-init-func panel)
	  popup (popup-init-func panel)
	  element (struct status-element uuid initial-data [tab popup] :begin)]
      (send status-agent
	    (fn [sa] (update-in sa [:active] assoc uuid element))))))

(defn status-update [uuid data]
  (send status-agent
	(fn [sa]
	  (update-in sa [:active uuid] assoc :data data :state :active)))
  data)

(defn status-complete [uuid]
  (send status-agent
	(fn [sa]
	  (let [element (assoc (-> sa :active (get uuid)) :state :completed)]
	    (update-in (update-in sa [:completed] conj element)
		       [:active] dissoc uuid)))))

(defn formatted-status-begin [uuid status-bar pattern init-data]
  (status-begin
   uuid status-bar
   (fn [parent]
     (let [label  (javax.swing.JLabel.)]
       (.add parent label)
       (fn [data] (doto label (.setText (apply format pattern data))))))
   (fn [_] (fn [_]))
   init-data))
