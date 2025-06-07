(ns emlyn.grid.operations
  "Operations that can be performed on a Grid."
  {:no-doc true}
  (:require [emlyn.grid.impl :as g :refer [->Grid]]))

(defn add-rows
  "Add rows to a grid.
   By default adds one row at the end of the grid.
   - `num`: the number of rows to add (default 1)
   - `pos`: the position to add the rows at (default end of grid)"
  [grid & {:keys [num pos] :or {num 1}}]
  (let [[w h] (.shape grid)
        pos (or pos h)]
    (->Grid [w (+ h num)]
            (reduce into
                    (subvec (.cells grid) 0 (* w pos))
                    [(repeat (* num w) nil)
                     (subvec (.cells grid) (* w pos))]))))

(defn add-cols
  "Add columns to a grid.
   By default adds one column at the end of the grid.
   - `num`: the number of columns to add (default 1)
   - `pos`: the position to add the columns at (default end of grid)"
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
  "Drop rows from a grid.
   By default drops one row from the end of the grid.
   - `num`: the number of rows to drop (default 1)
   - `pos`: the starting position to drop the rows from (default end of grid)"
  [grid & {:keys [num pos] :or {num 1}}]
  (let [[w h] (.shape grid)
        pos (or pos (- h num))
        num (min num (- h pos))]
    (->Grid [w (- h num)]
            (into (subvec (.cells grid) 0 (* w pos))
                  (subvec (.cells grid) (* w (+ pos num)))))))

(defn drop-cols
  "Drop columns from a grid.
   By default drops one column from the end of the grid.
   - `num`: the number of columns to drop (default 1)
   - `pos`: the starting position to drop the columns from (default end of grid)"
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
  "Map a function over the values of a grid (or several grids).
   If more than one grid is specified, they must all have the same width and height.
   The function should take one value for each grid mapped over,
   and return a single value that will be used in the returned grid."
  [fn grid & grids]
  (when-not (apply = (.shape grid) (map #(.shape %) grids))
    (throw (IllegalArgumentException. "Grids must all be the same shape")))
  (->Grid (.shape grid)
          (apply mapv fn
                 (.cells grid)
                 (map #(.cells %) grids))))

(defn map-kv
  "Map a function over the keys and values of a grid (or several grids).
   If more than one grid is specified, they must all have the same width and height.
   The function should accept one argument for the coordinate (as a vector [x y])
   and one argument for each grid's value at that coordinate.
   It should return a single value that will be used in the returned grid."
  [fn grid & grids]
  (when-not (apply = (.shape grid) (map #(.shape %) grids))
    (throw (IllegalArgumentException. "Grids must all be the same shape")))
  (->Grid (.shape grid)
          (apply mapv fn
                 (keys grid)
                 (.cells grid)
                 (map #(.cells %) grids))))

(defn concat-lr
  "Concatenate grids side by side (left to right).
   All grids must have the same height.
   Returns a new grid with the combined width of all grids and the same height."
  [grid & grids]
  (if (apply = (g/height grid) (map g/height grids))
    (->Grid [(apply + (g/width grid) (map g/width grids)) (g/height grid)]
            (->> (cons grid grids)
                 (map #(partition (g/width %) (.cells %)))
                 (apply map #(reduce into [] %&))
                 (reduce into)))
    (throw (IllegalArgumentException. "Grid heights must match."))))

(defn concat-tb
  "Concatenate grids top to bottom.
   All grids must have the same width.
   Returns a new grid with the combined height of all grids and the same width."
  [grid & grids]
  (if (apply = (g/width grid) (map g/width grids))
    (->Grid [(g/width grid) (apply + (g/height grid) (map g/height grids))]
            (reduce into (.cells grid) (map #(.cells %) grids)))
    (throw (IllegalArgumentException. "Grid widths must match."))))
