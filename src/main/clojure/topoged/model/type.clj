(ns topoged.model.type
  (:import
   (javax.persistence Entity Id Column Table GeneratedValue)
   ))

(definterface IType (getId []))

(deftype
  ^{Entity {} Table {:name="TYPE"} org.hibernate.annotations.Entity {:mutable false}} 
  TypeX [^Long id]
  IType
  (^{Id {} Column {:name "TYPE_ID"} GeneratedValue {}} getId [this] (.id this)) )

