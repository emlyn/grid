(ns emlyn.grid.algo
  (:require [emlyn.grid.type :refer [->Grid]]))

(defn map-vals
  [fn grid]
  (->Grid (.shape grid) (mapv fn (.cells grid))))

(defn map-kv
  [fn grid]
  (->Grid (.shape grid) (mapv fn (keys grid) (.cells grid))))


