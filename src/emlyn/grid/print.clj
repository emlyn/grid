(ns emlyn.grid.print
  (:require [emlyn.grid.type]
            [emlyn.grid.convert :refer [to-vecs]]
            [clojure.pprint])
  (:import [emlyn.grid.type Grid]))

(defmethod print-method Grid [grid writer]
  (print-method (to-vecs grid) writer))

(defmethod print-dup Grid [grid writer]
  (.write writer "#emlyn/grid ")
  (print-method (to-vecs grid) writer))

(defn pprint-grid
  [grid]
  (clojure.pprint/pprint (to-vecs grid)))

(. clojure.pprint/simple-dispatch addMethod Grid pprint-grid)
