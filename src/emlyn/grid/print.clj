(ns emlyn.grid.print
  (:require [emlyn.grid.type]
            [emlyn.grid.convert :refer [to-vecs]])
  (:import [emlyn.grid.type Grid]))

(defmethod print-method Grid [grid writer]
  (print-method (to-vecs grid) writer))

(defmethod print-dup Grid [grid writer]
  (.write writer "#emlyn/grid ")
  (print-method (to-vecs grid) writer))
