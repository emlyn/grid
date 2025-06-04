(ns emlyn.grid.impl
  {:no-doc true
   :clj-kondo/config '{:lint-as {potemkin/def-map-type clojure.core/deftype}}}
  (:require [emlyn.grid.everywhere]
            [clojure.string :as str]
            [potemkin :refer [def-map-type]])
  (:import [emlyn.grid.everywhere Everywhere]))

(def ^:dynamic *index-mode*
  "See `with-index-mode` for valid mode values."
  :strict)

(defmacro with-index-mode
  "How to handle indices outside the usual bounds. Valid mode values are:
   - `:strict` (default): no special handling of indices
   - `:wrap`: indices wrap around the edges of the grid
   - `:clamp`: indices clamp to the edges of the grid
   - `:python`: negative indices count from the end of the grid
   Indices that still fall outside the grid after this handling will
   throw an exception if you try to set (e.g. `assoc`) them,
   and will return the default value (or `nil`) when you read them."
  [mode & body]
  `(binding [*index-mode* ~mode]
     ~@body))

(defn set-index-mode!
  "Permanently sets the default index mode. See `with-index-mode`"
  [mode]
  (alter-var-root #'*index-mode* (constantly mode)))

(defn- pos->index
  "Get the index of a position in a grid."
  [[w h] [x y]]
  (let [[x y]
        (case *index-mode*
          :python [(if (neg? x) (+ w x) x)
                   (if (neg? y) (+ h y) y)]
          :wrap [(mod x w)
                 (mod y h)]
          :clamp [(max 0 (min x (dec w)))
                  (max 0 (min y (dec h)))]
          [x y])]
    (when (and (< -1 x w)
               (< -1 y h))
      (+ x (* w y)))))

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

(defn- associative-data?
  "Is the init data a valid associative (map or vector) of y to associative of x to value?"
  [data]
  (and (associative? data)
       (or (vector? data)
           (every? nat-int? (keys data)))
       (every? #(and (associative? %)
                     (or (vector? %)
                         (every? nat-int? (keys %))))
               (if (vector? data)
                 data
                 (vals data)))))

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
             (seq? i) (Grid. shape (if (instance? Everywhere val)
                                     (reduce #(assoc %1 %2 (val nil))
                                             cells
                                             i)
                                     (reduce (fn [c [i v]]
                                               (assoc c i v))
                                             cells
                                             (map vector i (concat val (repeat nil))))))
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
  [coll n]
  (= n (count (take (inc n) coll))))

(defn- infer-shape
  "Infer the shape of a grid from its init data."
  [data]
  (cond
    (instance? Grid data)
    (.shape data)

    (string? data)
    (recur (str/split-lines data))

    (and (sequential? data)
         (every? #(or (sequential? %) (string? %)) data))
    [(apply max 0 (map count data))
     (count data)]

    (map-data? data)
    (->> (keys data)
         (reduce (partial map max) [-1 -1])
         (mapv inc))

    (associative-data? data)
    [(apply max 0 (map #(if (vector? %)
                          (count %)
                          (inc (apply max -1 (keys %))))
                       (if (vector? data)
                         data
                         (vals data))))
     (if (vector? data)
       (count data)
       (inc (apply max -1 (keys data))))]))

(defn- massage
  "Massage init data into a vector of cells."
  [w h data]
  (cond
    ;; Unspecified data gets filled with nil
    (nil? data)
    (into [] (repeat (* w h) nil))

    (string? data)
    (let [lines (str/split-lines data)]
      (cond
        (> (count lines) 1)      (recur w h lines)
        (= (* w h) (count data)) (vec data)
        (= h 1)                  (recur w h lines)
        :else
        (throw (IllegalArgumentException. "Invalid init data."))))

    (instance? Everywhere data)
    (into [] (repeat (* w h) (data nil)))

    (instance? Grid data)
    (cond
      (= [w h] (.shape data))
      (.cells data)

      (and (= w (width data))
           (< h (height data)))
      (subvec (.cells data) 0 (* w h))

      (= w (width data))
      (into (.cells data) (repeat (* w (- h (height data))) nil))

      :else
      (reduce into []
              (take h
                    (concat (map #(take w (concat % (repeat nil))) (partition (first (.shape data)) (.cells data)))
                            (repeat (repeat w nil))))))

    ;; A fn gets applied to the coordinates
    (fn? data)
    (mapv (partial apply data) (shape->keys [w h]))

    ;; Map of xy coordinate to value
    (map-data? data)
    (reduce-kv (fn [cells pos val]
                 (if-let [i (pos->index [w h] pos)]
                   (assoc cells i val)
                   cells))
               (into [] (repeat (* w h) nil))
               data)

    ;; Associative of y to associative of x to value
    (associative-data? data)
    (reduce-kv (fn [cells y xval]
                 (reduce-kv (fn [cells x val]
                              (if-let [i (pos->index [w h] [x y])]
                                (assoc cells i val)
                                cells))
                            cells
                            xval))
               (into [] (repeat (* w h) nil))
               data)

    ;; A flat sequence of strings of exactly the right length
    (and (sequential? data)
         (count= data (* w h))
         (every? string? data))
    (vec data)

    ;; A sequence of sequences or strings
    (and (sequential? data)
         (every? #(or (sequential? %) (string? %))
                 (take h data)))
    (reduce into []
            (take h
                  (concat (map #(take w (concat % (repeat nil))) data)
                          (repeat (repeat w nil)))))

    ;; Finally a flat sequence of values of exactly the right length.
    ;; This must be last, as it mustn't take precedence over seq-of-seqs
    ;; if the values themselves are sequential.
    (and (sequential? data)
         (count= data (* w h)))
    (vec data)

    :else
    (throw (IllegalArgumentException. "Invalid init data."))))

(defn grid
  "Construct a grid."
  ([data]
   (if-let [[w h] (infer-shape data)]
     (grid w h data)
     (throw (IllegalArgumentException.
             "Unable to infer shape from init data."))))
  ([w h & [data]]
   (->Grid [w h] (massage w h data))))
