(ns emlyn.grid.type
  {:clj-kondo/config '{:lint-as {potemkin/def-map-type clojure.core/deftype}}}
  (:require [clojure.string :as str]
            [potemkin :refer [def-map-type]]))

(defn- pos->index
  [[w h] [x y]]
  (when (and (< -1 x w)
             (< -1 y h))
    (+ x (* w y))))

(defn- shape->keys
  [[w h]]
  (for [y (range h)
        x (range w)]
    [x y]))

(defn- massage
  [w h data]
  (if (fn? data)
    (mapv (partial apply data) (shape->keys [w h]))
    (let [data (if (string? data)
                 (str/split-lines data)
                 data)]
      (reduce into
              []
              (take h
                    (concat (map #(take w (concat % (repeat nil))) data)
                            (repeat (repeat w nil))))))))

(def-map-type Grid [shape cells]
  (get [_ pos default] (get cells (pos->index shape pos) default))
  (assoc [_ pos val] (if-let [i (pos->index shape pos)]
                       (Grid. shape (assoc cells i val))
                       (throw (IndexOutOfBoundsException.))))
  (dissoc [this pos] (if-let [i (pos->index shape pos)]
                       (Grid. shape (assoc cells i nil))
                       this))
  (keys [_] (shape->keys shape))
  (meta [_] (meta shape))
  (with-meta [_ m] (Grid. (with-meta shape m) cells))
  (count [_] (apply * shape)))

(defn width
  [grid]
  (first (.shape grid)))

(defn height
  [grid]
  (second (.shape grid)))

(defn as-rows
  [grid]
  (partition (width grid) (.cells grid)))

(defn grid
  ([init]
   (cond
     (string? init)
     (grid (str/split-lines init))

     (and (sequential? init)
          (every? #(or (sequential? %) (string? %)) init))
     (if (apply = (map count init))
       (->Grid [(count (first init))
                (count init)]
               (reduce into [] init))
       (throw (IllegalArgumentException.
               "All rows must have the same length.")))

     :else
     (throw (IllegalArgumentException.
             "Init value for grid must be a sequence of sequences or a string."))))
  ([w h & [init]]
   (->Grid [w h] (massage w h init))))
