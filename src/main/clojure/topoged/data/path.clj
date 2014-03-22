(ns topoged.data.path
  (:require [archimedes.vertex :as v])
  (:use [topoged.data.schema]))

;;;
;;; Define Schema
;;;

;; Conclusions

(defnode Individual :individual 
  [
   :name ;the print name1
   ])

(defnode Birth :birth 
  [
   ])

(defnode Marriage :marriage 
  [
   ])

;; Evidence

(defnode Source :source 
  [
   ])

(defnode Persona :Persona 
  [
   ])

(defnode Event :marriage 
  [
   ])


; Conclusion

(defedge Parent Birth :parent Individual 
  [
   :order ; 0 = father, 1 = mother
   ])

(defedge Child Birth :child Individual 
  [
   :order ; Order of the child in the family
   ])

(defedge Spouse Marriage :spouse Individual 
  [
   :order ; 0 = husband and 1 = wife
   ])

; Evidence

(defedge EventSource Event :source Source [])
(defedge EventPersona Event :persona Persona [])
(defedge IndividualPersona Individual :evidence Persona [])
(defedge BirthEvent Birth :evidence Event [])

;;
;; Define paths

(def child->parent-path [ Individual <-Child- Birth -Parent-> Individual ])

(def marriage-path [ Individual <-Spouse- Marriage -Spouse-> Individual ])



(defn add-parents [db child father mother order]
  (when child
    (let [template [child {:order order} {} {:order 0} father]
          [child e1 birth _ _] (if father
                                 (path-create db child->parent-path template)
                                 template)]
      (when mother
        (path-create db child->parent-path 
                     [child e1 birth {:order 1} mother])))))

(defn parents-of [node]
  (let [m (reduce conj {}
                  (for [[_ _ _ edge parent] (path-query child->parent-path node)]
                    [(v/get edge :order) parent]))]
    [(get m 0) (get m 1)]))

(defn add-spouse [db s1 s2 ]
  (path-create db marriage-path [ s1 {:order 0} {} {:order 1} s2]))
