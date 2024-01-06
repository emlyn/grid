(ns emlyn.grid.convert
  (:require [emlyn.grid.impl :refer [width]]))

(defn to-vec
  "Convert the grid to a flat vector of all values (left to right, top to bottom)."
  [grid]
  (.cells grid))

(defn to-vecs
  "Convert the grid to a vector of vectors, one for each row."
  [grid]
  (let [cells (.cells grid)
        w (width grid)]
    (mapv #(subvec cells % (+ % w))
          (range 0 (count cells) w))))

(defn to-map
  "Convert the grid to a plain map of coordinate to value."
  [grid & {:keys [filter] :or {filter some?}}]
  (reduce-kv (fn [m k v]
               (if (or (nil? filter)
                       (filter v))
                 (assoc m k v)
                 m))
             {}
             grid))

(defn to-maps
  "Convert the grid to a map of x to map of y to value."
  [grid & {:keys [filter] :or {filter some?}}]
  (reduce-kv (fn [m k v]
               (if (or (nil? filter)
                       (filter v))
                 (assoc-in m k v)
                 m))
             {}
             grid))
