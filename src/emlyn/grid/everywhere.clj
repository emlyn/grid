(ns emlyn.grid.everywhere
  "Implementation of the Everywhere helper type."
  {:no-doc true
   :clj-kondo/config '{:lint-as {potemkin/def-map-type clojure.core/deftype}}}
  (:require [potemkin :refer [def-map-type]]))

(def-map-type Everywhere [val]
  (get [_ _key _default] val)
  (assoc [_ _key val] (Everywhere. val))
  (dissoc [_ _key] (Everywhere. nil))
  (keys [_] [nil])
  (meta [_] (meta val))
  (with-meta [_ m] (Everywhere. (with-meta val m))))

(defn everywhere
  "Represents an unbounded grid with the same value everywhere.
   This is useful when initialising a new grid with a default value,
   or when setting all cells in an area of an existing grid to the same value.

   ```
   (grid 4 4 (everywhere 0))
   ```
   ```
   (assoc grid [[0 4] [0 2]] (everywhere 42))
   ```
   "
  [val]
  (->Everywhere val))
