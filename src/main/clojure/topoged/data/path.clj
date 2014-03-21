(ns topoged.data.path
  (:use [topoged.data.schema]))

;;;
;;; Define Schema
;;;

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

(def child->parent-path [ Individual <-Child- Birth -Parent-> Individual ])

(def marriage-path [ Individual <-Spouse- Marriage -Spouse-> Individual ])
