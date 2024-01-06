(ns emlyn.grid.transforms
  (:require [emlyn.grid.impl :as g :refer [->Grid]]))

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
