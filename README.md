# grid

A library for working with 2D rectangular arrays of data.

[![Clojars Project](https://img.shields.io/clojars/v/com.github.emlyn/grid.svg)](https://clojars.org/com.github.emlyn/grid)
[![Downloads](https://img.shields.io/clojars/dt/com.github.emlyn/grid.svg)](https://clojars.org/com.github.emlyn/grid)
[![cljdoc](https://cljdoc.org/badge/com.github.emlyn/grid)](https://cljdoc.org/d/com.github.emlyn/grid)

## Motivation

Inspired by Advent of Code.
I found that I was often dealing with two dimensional grids of data,
and writing a load of ad-hoc functions for dealing with them.
So I decided to write this library that defines Grid,
a map-like type that takes pairs of coordinates as keys,
but stores the data internally in a flat vector.
It makes it easier to work with 2-dimensional grids of values.

## Quickstart

The full [project documentation](https://cljdoc.org/d/com.github.emlyn/grid) can be found on cljdoc. Here is a quick summary to get you started.

### Installation

Add the latest version of `com.github.emlyn/grid` to your dependencies:
![cljdoc](https://clojars.org/com.github.emlyn/grid/latest-version.svg)

Require the `emlyn.grid` namespace:
```clojure
(require '[emlyn.grid :as g])
```

### Usage

To construct a grid, you use the `grid` function.
It accepts a wide range of data formats that it will convert into a grid.
For some of these you will need to supply the width and height of the grid you want,
and for others this is optional, as the dimensions can be inferred from the data.

Here are a few of the ways a grid can be constructed:

```clojure
(g/grid "abc\ndef\nghi") ;; constructs a 3x3 grid of characters.
(g/grid [[2 4 6] [1 3 5]]) ;; constructs a 3x2 grid of numbers.
(g/grid 4 4 *) ;; constructs a 4x4 grid where each cell is the product of the x and y coordinates.
(g/grid 5 5 (g/everywhere 99)) ;; constructs a 5x5 grid with 99 in every cell.
#emlyn/grid [[1 2 3] [4 5 6]] ;; Construct a grid using the reader macro.
```

You can print a grid as a text table using `print-table`:

```clojure
(def board (g/grid [[1 2 3] [4 5 6] [7 8 9]]))

(g/print-table board)
;; =>
;; 1|2|3
;; 4|5|6
;; 7|8|9
```

To get or set individual values in a grid, treat it like a Clojure map with `[x y]` keys:

```clojure
(def board (g/grid [[1 2 3] [4 5 6] [7 8 9]]))

(board [1 2]) ;; => 8
(get board [1 4] :missing) ;; => :missing

(-> board (assoc [0 0] 99) g/print-table)
;; =>
;; 99|2|3
;;  4|5|6
;;  7|8|9
```

In addition, there are some extra functions provided for doing other things with grids, such as:
`width` and `height` for getting their size;
`add-rows`, `add-cols`, `drop-rows` and `drop-cols` for adding or removing rows and columns;
`map-vals` and `map-kv` for mapping a function of the values (or keys and values) over a grid;
`transpose`, `rotate-right`, etc. for transforming grids.

## License

Copyright © 2023 Emlyn Corrin

Distributed under the Eclipse Public License version 1.0.
