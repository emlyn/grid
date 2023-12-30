(ns emlyn.grid.print
  (:require [emlyn.grid.type]
            [emlyn.grid.convert :refer [to-rows]])
  (:import [emlyn.grid.type Grid]))

(defmethod print-method Grid [grid writer]
  (print-method (to-rows grid) writer))

(defmethod print-dup Grid [grid writer]
  (.write writer "#emlyn/grid ")
  (print-method (to-rows grid) writer))
