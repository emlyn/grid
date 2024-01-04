(ns emlyn.grid.impl
  {:clj-kondo/config '{:lint-as {potemkin/def-map-type clojure.core/deftype}}}
  (:require [clojure.string :as str]
            [potemkin :refer [def-map-type]]))

(defn- pos->index
  "Get the index of a position in a grid."
  [[w h] [x y]]
  (when (and (< -1 x w)
             (< -1 y h))
    (+ x (* w y))))

(defn slice->range
  ([limit] (range limit))
  ([_ v] [v])
  ([_ lo hi] (range lo hi)))

(defn- slice->indices
  [[w h] [x y]]
  (case [(int? x) (int? y)]
    [true true] (pos->index [w h] [x y])
    [true false] (map #(pos->index [w h] [x %]) (apply slice->range h y))
    [false true] (map #(pos->index [w h] [% y]) (apply slice->range w x))
    [false false]
    (let [ry (apply slice->range h y)
          rx (apply slice->range w x)]
      [(count rx)
       (count ry)
       (for [y ry
             x rx]
         (pos->index [w h] [x y]))])))

(defn shape->keys
  "Get the keys of a grid."
  [[w h]]
  (for [y (range h)
        x (range w)]
    [x y]))

(defn- map-data?
  "Is the init data a valid map of coordinate to value?"
  [data]
  (and (map? data)
       (every? #(and (sequential? %)
                     (= 2 (count %))
                     (every? nat-int? %)) (keys data))))

(defn mapmap-data?
  "Is the init data a valid map of x to map of y to value?"
  [data]
  (and (map? data)
       (every? nat-int? (keys data))
       (every? #(and (map? %)
                     (every? nat-int? (keys %))) (vals data))))

(declare grid)

(def-map-type Grid [shape cells]
  (get [_ pos default]
       (let [i (slice->indices shape pos)]
         (cond
           (nil? i) default
           (int? i) (get cells i default)
           (seq? i) (mapv #(get cells % default) i)
           :else
           (let [[w h d] i]
             (Grid. [w h] (mapv #(get cells % default) d))))))
  (assoc [_ pos val]
         (let [i (slice->indices shape pos)]
           (cond
             (nil? i) (throw (IndexOutOfBoundsException.))
             (int? i) (Grid. shape (assoc cells i val))
             (seq? i) (Grid. shape (reduce (fn [c [i v]]
                                             (assoc c i v))
                                           cells
                                           (map vector i (concat val (repeat nil)))))
             :else    (let [[w h d] i
                            g (grid w h val)]
                        (Grid. shape
                               (reduce (fn [c [i v]]
                                         (assoc c i v))
                                       cells
                                       (map (fn [i [x y]]
                                              [i (get g [x y])]) d (shape->keys [w h]))))))))
  (dissoc [this pos]
          (let [i (slice->indices shape pos)]
            (cond
              (nil? i) this
              (int? i) (Grid. shape (assoc cells i nil))
              (seq? i) (Grid. shape (reduce (fn [c i] (assoc c i nil)) cells i))
              :else    (let [[_ _ d] i]
                         (Grid. shape (reduce #(assoc %1 %2 nil) cells d))))))
  (keys [_] (shape->keys shape))
  (meta [_] (meta shape))
  (with-meta [_ m] (Grid. (with-meta shape m) cells))
  (count [_] (apply * shape)))

(defn width
  "The width of a grid in cells."
  [grid]
  (first (.shape grid)))

(defn height
  "The height of a grid in cells."
  [grid]
  (second (.shape grid)))

(defn- count=
  "Check if a collection is a certain size without realizing too much of it (e.g. if it's infinite)."
  [n col]
  (= n (count (take (inc n) col))))

(defn- infer-shape
  "Infer the shape of a grid from its init data."
  [data]
  (cond
    (and (sequential? data)
         (every? #(or (sequential? %) (string? %)) data)
         (or (empty? data) (apply = (map count data))))
    [(count (first data))
     (count data)]

    (map-data? data)
    (->> (keys data)
         (reduce (partial map max) [-1 -1])
         (mapv inc))

    (mapmap-data? data)
    [(inc (apply max -1 (keys data)))
     (inc (apply max -1 (mapcat keys (vals data))))]))

(defn- massage
  "Massage init data into a vector of cells."
  [w h data]
  (cond
    ;; Unspecified data gets filled with nil
    (nil? data)
    (into [] (repeat (* w h) nil))

    (instance? Grid data)
    (if (= [w h] (.shape data))
      (.cells data)
      (reduce into
              []
              (take h
                    (concat (map #(take w (concat % (repeat nil))) (partition (first (.shape data)) (.cells data)))
                            (repeat (repeat w nil))))))

    ;; A fn gets applied to the coordinates
    (fn? data)
    (mapv (partial apply data) (shape->keys [w h]))

    ;; Map of coordinate to value
    (map-data? data)
    (reduce-kv (fn [cells pos val]
                 (if-let [i (pos->index [w h] pos)]
                   (assoc cells i val)
                   cells))
               (into [] (repeat (* w h) nil))
               data)

    ;; Map of x to map of y to value
    (mapmap-data? data)
    (reduce-kv (fn [cells x ymap]
                 (reduce-kv (fn [cells y val]
                              (if-let [i (pos->index [w h] [x y])]
                                (assoc cells i val)
                                cells))
                            cells
                            ymap))
               (into [] (repeat (* w h) nil))
               data)

    :else
    (let [split-data (if (string? data)
                       (str/split-lines data)
                       data)]
      (cond
        ;; A sequence of sequences, or a newline-separated string
        (and (sequential? split-data)
             (every? #(or (sequential? %) (string? %))
                     (take h split-data)))
        (reduce into []
                (take h
                      (concat (map #(take w (concat % (repeat nil))) split-data)
                              (repeat (repeat w nil)))))

        ;; Finally a flat sequence of values of exactly the right length.
        ;; This must be last, as it mustn't take precedence over seq-of-seqs.
        (and (or (sequential? data) (string? data))
             (count= (* w h) data))
        (vec data)

        :else
        (throw (IllegalArgumentException. "Invalid init data."))))))

(defn grid
  "Construct a grid."
  ([data]
   (cond
     (instance? Grid data) data
     (string? data) (grid (str/split-lines data))
     :else
     (if-let [[w h] (infer-shape data)]
       (grid w h data)
       (throw (IllegalArgumentException.
               "Unable to infer shape from init data.")))))
  ([w h & [data]]
   (->Grid [w h] (massage w h data))))
