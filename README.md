# grid

A library for working with 2D rectangular arrays of data.

[![Clojars Project](https://img.shields.io/clojars/v/com.github.emlyn/grid.svg)](https://clojars.org/com.github.emlyn/grid)

## Documentation

[Project documentation](https://cljdoc.org/d/com.github.emlyn/grid) can be found on cljdoc.

## Usage

Inspired by Advent of Code.
I found that I was often dealing with two dimensional grids of data,
and writing a load of ad-hoc functions for dealing with them.
So I decided to write a library around a map-like type,
that takes pairs of coordinates as keys,
but stores the data internally in a flat vector.

``` clojure
(require '[emlyn.grid :as g])

(def board
  (->> (g/grid 3 3 (g/everywhere :empty))
       (g/map-kv (fn [[x y :as coord] val] (if (= coord [2 2]) :feature val)))))

(g/print-table board)
;; =>
;; :empty|:empty|  :empty
;; :empty|:empty|  :empty
;; :empty|:empty|:feature

(-> board g/to-map (get [2 2])) ;=> :feature

(-> board g/rotate-left g/to-map (get [0 0])) ;=> :empty
(-> board g/rotate-left g/to-map (get [2 0])) ;=> :feature
```

## License

Copyright © 2023 Emlyn Corrin

Distributed under the Eclipse Public License version 1.0.
