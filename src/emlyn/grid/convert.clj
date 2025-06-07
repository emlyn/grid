(ns emlyn.grid.convert
  "Functions for converting a grid into other data structures."
  {:no-doc true}
  (:require [emlyn.grid.impl :refer [rows]]))

(defn to-vec
  "Convert a grid to a flat vector of all values (left to right, top to bottom).
   This is the same as calling `vals` on the grid.

   ```
   (to-vec grid)
   ```
   "
  [grid]
  (.cells grid))

(defn to-vecs
  "Convert a grid to a vector of vectors, one for each row.
   Similar to calling [emlyn.grid/rows] on the grid.

   ```
   (to-vecs grid)
   ```
   "
  [grid]
  (vec (rows grid)))

(defn to-map
  "Convert a grid to a map of xy-coordinate to value
   optionally filtering out some cells (by default nils).

   ```
   (to-map grid :filter #(not= % 0))
   ```
   "
  [grid & {:keys [filter] :or {filter some?}}]
  (reduce-kv (fn [m k v]
               (if (or (nil? filter)
                       (filter v))
                 (assoc m k v)
                 m))
             {}
             grid))

(defn to-maps
  "Convert a grid to a map of y (or x) coordinate to map of x (or y) coordinate to value,
   optionally filtering out some cells (by default nils).
   By default uses y as the first coordinate to match the format used in the constructor,
   pass in `:xy-order true` to use x as the first coordinate.

   ```
   (to-grids grid :xy-order true :filter (partial not= 0))
   ```
   "
  [grid & {:keys [filter xy-order] :or {filter some?}}]
  (reduce-kv (fn [m k v]
               (if (or (nil? filter)
                       (filter v))
                 (assoc-in m (if xy-order k (reverse k)) v)
                 m))
             {}
             grid))
