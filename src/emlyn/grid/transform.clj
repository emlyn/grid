(ns emlyn.grid.transform
  (:require [emlyn.grid.type :as g :refer [->Grid]]))

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

(defn transpose
  "Transpose a grid."
  [grid]
  (let [[h w] (.shape grid)]
    (->Grid [w h] (mapv (fn [[x y]] (grid [y x]))
                        (g/shape->keys [w h])))))

(defn rotate-right
  "Rotate a grid 90 degrees clockwise."
  [grid]
  (let [[h w] (.shape grid)]
    (->Grid [w h] (mapv (fn [[x y]] (grid [y (- w x 1)]))
                        (g/shape->keys [w h])))))

(defn rotate-left
  "Rotate a grid 90 degrees anticlockwise."
  [grid]
  (let [[h w] (.shape grid)]
    (->Grid [w h] (mapv (fn [[x y]] (grid [(- h y 1) x]))
                        (g/shape->keys [w h])))))

(defn rotate-180
  "Rotate a grid 180 degrees."
  [grid]
  (let [[w h] (.shape grid)]
    (->Grid [w h] (mapv (fn [[x y]] (grid [(- w x 1) (- h y 1)]))
                        (g/shape->keys [w h])))))

(defn flip-lr
  "Flip a grid left to right."
  [grid]
  (let [[w h] (.shape grid)]
    (->Grid [w h] (mapv (fn [[x y]] (grid [(- w x 1) y]))
                        (g/shape->keys [w h])))))

(defn flip-tb
  "Flip a grid top to bottom."
  [grid]
  (let [[w h] (.shape grid)]
    (->Grid [w h] (mapv (fn [[x y]] (grid [x (- h y 1)]))
                        (g/shape->keys [w h])))))

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
