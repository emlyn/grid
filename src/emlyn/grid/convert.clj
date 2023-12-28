(ns emlyn.grid.convert
  (:require [emlyn.grid.type :refer [width]]))

(defn to-rows
  "Convert the grid to a sequence of rows."
  [grid]
  (partition (width grid) (.cells grid)))

(defn ^{:deprecated "0.2.0"
        :superseded-by "to-rows"} as-rows
  "DEPRECATED: Use `to-rows` instead."
  [grid]
  (to-rows grid))

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

(defn to-map-of-maps
  "Convert the grid to a map of x to map of y to value."
  [grid & {:keys [filter] :or {filter some?}}]
  (reduce-kv (fn [m k v]
               (if (or (nil? filter)
                       (filter v))
                 (assoc-in m k v)
                 m))
             {}
             grid))
