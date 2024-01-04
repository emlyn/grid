(ns emlyn.grid.everywhere
  {:clj-kondo/config '{:lint-as {potemkin/def-map-type clojure.core/deftype}}}
  (:require [potemkin :refer [def-map-type]]))

(def-map-type Everywhere [val]
  (get [_ _key _default] val)
  (assoc [_ _key val] (Everywhere. val))
  (dissoc [_ _key] (Everywhere. nil))
  (keys [_] [nil])
  (meta [_] (meta val))
  (with-meta [_ m] (Everywhere. (with-meta val m))))

(defn everywhere
  [val]
  (->Everywhere val))
