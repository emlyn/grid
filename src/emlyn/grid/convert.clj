(ns emlyn.grid.convert
  (:require [emlyn.grid.impl :refer [width]]))

(defn to-vec
  "Convert a grid to a flat vector of all values (left to right, top to bottom)."
  [grid]
  (.cells grid))

(defn to-vecs
  "Convert a grid to a vector of vectors, one for each row."
  [grid]
  (let [cells (.cells grid)
        w (width grid)]
    (mapv #(subvec cells % (+ % w))
          (range 0 (count cells) w))))

(defn to-map
  "Convert a grid to a map of xy-coordinate to value
   optionally filtering out some cells (by default nils)."
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
   pass in `:xy-order true` to use x as the first coordinate."
  [grid & {:keys [filter xy-order] :or {filter some?}}]
  (reduce-kv (fn [m k v]
               (if (or (nil? filter)
                       (filter v))
                 (assoc-in m (if xy-order k (reverse k)) v)
                 m))
             {}
             grid))
