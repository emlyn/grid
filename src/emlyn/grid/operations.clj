(ns emlyn.grid.operations
  {:no-doc true}
  (:require [emlyn.grid.impl :as g :refer [->Grid]]))

(defn add-rows
  [grid & {:keys [num pos] :or {num 1}}]
  (let [[w h] (.shape grid)
        pos (or pos h)]
    (->Grid [w (+ h num)]
            (reduce into
                    (subvec (.cells grid) 0 (* w pos))
                    [(repeat (* num w) nil)
                     (subvec (.cells grid) (* w pos))]))))

(defn add-cols
  [grid & {:keys [num pos] :or {num 1}}]
  (let [[w h] (.shape grid)
        pos (or pos w)]
    (->Grid [(+ w num) h]
            (->> (.cells grid)
                 (partition w)
                 (map #(reduce into
                               []
                               [(take pos %)
                                (repeat num nil)
                                (drop pos %)]))
                 (reduce into [])))))

(defn drop-rows
  [grid & {:keys [num pos] :or {num 1}}]
  (let [[w h] (.shape grid)
        pos (or pos (- h num))
        num (min num (- h pos))]
    (->Grid [w (- h num)]
            (into (subvec (.cells grid) 0 (* w pos))
                  (subvec (.cells grid) (* w (+ pos num)))))))

(defn drop-cols
  [grid & {:keys [num pos] :or {num 1}}]
  (let [[w h] (.shape grid)
        pos (or pos (- w num))
        num (min num (- w pos))]
    (->Grid [(- w num) h]
            (->> (.cells grid)
                 (partition w)
                 (map #(reduce into
                               []
                               [(take pos %)
                                (drop (+ pos num) %)]))
                 (reduce into [])))))

(defn map-vals
  "Map a function over the values of a grid."
  [fn grid & grids]
  (when-not (apply = (.shape grid) (map #(.shape %) grids))
    (throw (IllegalArgumentException. "Grids must all be same shape")))
  (->Grid (.shape grid)
          (apply mapv fn
                 (.cells grid)
                 (map #(.cells %) grids))))

(defn map-kv
  "Map a function over the keys and values of a grid."
  [fn grid & grids]
  (when-not (apply = (.shape grid) (map #(.shape %) grids))
    (throw (IllegalArgumentException. "Grids must all be same shape")))
  (->Grid (.shape grid)
          (apply mapv fn
                 (keys grid)
                 (.cells grid)
                 (map #(.cells %) grids))))

(defn concat-lr
  [grid & grids]
  (if (apply = (g/height grid) (map g/height grids))
    (->Grid [(apply + (g/width grid) (map g/width grids)) (g/height grid)]
            (->> (cons grid grids)
                 (map #(partition (g/width %) (.cells %)))
                 (apply map #(reduce into [] %&))
                 (reduce into)))
    (throw (IllegalArgumentException. "Grid heights must match."))))

(defn concat-tb
  [grid & grids]
  (if (apply = (g/width grid) (map g/width grids))
    (->Grid [(g/width grid) (apply + (g/height grid) (map g/height grids))]
            (reduce into (.cells grid) (map #(.cells %) grids)))
    (throw (IllegalArgumentException. "Grid widths must match."))))
