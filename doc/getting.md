# Getting & setting values in a grid

Grids behave like clojure maps, where the keys are 2-element vectors of x-y coordinates.
All the usual clojure core functions (`assoc`, `dissoc`, `keys`, `vals`, `reduce-kv` etc.)
should work as expected with them.

## Getting

You can get values just like you would with a normal Clojure map:

```clojure
(def board (g/grid [[1 2 3] [4 5 6] [7 8 9]]))

(board [1 2]) ;; => 8

(get board [2 1]) ;; => 5
```

## Setting

As a grid behaves like a Clojure map, you can use `assoc` to set a value.
As you can't remove a value completely, `dissoc` will set the value to `nil`.

```clojure
(def board (g/grid [[1 2 3] [4 5 6] [7 8 9]]))

(-> board g/print-table)
;; =>
;; 1|2|3
;; 4|5|6
;; 7|8|9

(-> board (assoc [1 1] 0) g/print-table)
;; =>
;; 1|2|3
;; 4|0|6
;; 7|8|9

(-> board (dissoc [1 1]) g/print-table)
;; =>
;; 1|2|3
;; 4| |6
;; 7|8|9
```

## Slices

When indexing into a grid, as well as getting an individual cell with `[x y]`,
you can also get a horizontal or vertical slice, or a subgrid,
by passing in a vector of two numbers for `x` and/or `y`.

```clojure
(def board (g/grid 4 4 (range 16)))
(g/print-table board)
;; =>
;;  0| 1| 2| 3
;;  4| 5| 6| 7
;;  8| 9|10|11
;; 12|13|14|15

;; Get the complete row at x=1:
(board [[] 1]) ;; => [4 5 6 7]

;; Get the complete column at y=2:
(board [2 []]) ;; => [2 6 10 14]

;; Get the central 2x2 grid:
(g/print-table (board [[1 3] [1 3]]))
;; =>
;; 5| 6
;; 9|10

;; Set column x=3 to all `:x`:
(g/print-table (assoc board [2 []] (g/everywhere :x)))
;;  0| 1|:x| 3
;;  4| 5|:x| 7
;;  8| 9|:x|11
;; 12|13|:x|15
```

## Out of bound indices

By default, getting any out-of-bounds value from a grid will return `nil`,
and trying to set one will throw an `IndexOutOfBounds` exception.
However, you can configure this behaviour by setting the index mode
using `with-index-mode` or `set-index-mode!`, passing one of the following keywords:
- `:strict` (default): no special handling of indices
- `:wrap`: indices wrap around the edges of the grid
(so if the grid were 10 cells wide, an x index of 12 would map to x=2)
- `:clamp`: indices clamp to the edges of the grid
(so if the grid were 10 cells wide, an x index of 12 would map to x=9)
- `:python`: negative indices count back from the end of the grid, like in Python
(so if the grid were 10 cells wide, an x index of -2 would map to x=8)

Indices that still fall outside the grid after this handling will
throw an exception if you try to set them (e.g. using `assoc`),
and will return the default value (or `nil`) when you read them.

```clojure
(def board (g/grid [[1 2 3] [4 5 6] [7 8 9]]))

(get board [2 4]) ;; => nil
(get board [2 4] :err) ;; => :err
(assoc board [2 4] 42) ;; => throws IndexOutOfBoundsException

(g/with-index-mode :wrap
    (get board [2 4]) ;; => 6
    (g/print-table (assoc board [2 4] 42))
    ;; =>
    ;; 1|2| 3
    ;; 4|5|42
    ;; 7|8| 9
    )

(g/set-index-mode! :clamp)
(get board [2 4]) ;; => 9
(g/print-table (assoc board [2 4] 42))
;; =>
;; 1|2| 3
;; 4|5| 6
;; 7|8|42

(g/with-index-mode :python
    (get board [-1 -1]) ;; => 9
    (get board [-1 -4]) ;; => nil
    (g/print-table (assoc board [-2 -1] 42))
    ;; =>
    ;; 1| 2|3
    ;; 4| 5|6
    ;; 7|42|9
    )
```
