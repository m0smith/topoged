(ns topoged.data.inmemory
  (:use [clojure.core.logic :rename {== ?==}])
  (:use [topoged.util])
 )

;;
;; To implement a new data namespace, it is only necessary to implement the following the
;; functions that reference "data-store", namely:
;;   add-to-data-store - this function accepts the given map and adds it to the data store.
;;   indatastoreo      - expects the data-store to look like a seq.  This could be done by
;;                       pre-searching the database or if using Datomic see
;;        https://github.com/clojure/core.logic/wiki/Extending-core.logic-%28Datomic-example%29



;; Database Specific stuff

(in-ns 'topoged.data.common)

(def data-store (atom []))

(defn indatastoreo [pm]
  (membero pm @data-store))

(defn add-to-data-store [m]
  (swap! data-store conj m))
