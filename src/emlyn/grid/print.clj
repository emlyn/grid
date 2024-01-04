(ns emlyn.grid.print
  (:require [emlyn.grid.impl]
            [emlyn.grid.convert :refer [to-vecs]]
            [clojure.pprint])
  (:import [emlyn.grid.impl Grid]))

(defmethod print-method Grid [grid writer]
  (print-method (to-vecs grid) writer))

(defmethod print-dup Grid [grid writer]
  (.write writer "#emlyn/grid ")
  ;; By default, to-vecs creates subvectors, which print differently,
  ;; so convert them to plain vectors for printing:
  (print-dup (mapv #(into [] %)
                   (to-vecs grid))
             writer))

(defn pprint-grid
  [grid]
  (clojure.pprint/pprint (to-vecs grid)))

(. clojure.pprint/simple-dispatch addMethod Grid pprint-grid)
