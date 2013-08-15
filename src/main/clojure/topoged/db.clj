(ns topoged.db
  (:require [archimedes.edge :as e]))

(def add-edge (partial e/connect-with-id! nil))
