# Displaying grids

You can use `print-table` to display a grid in various formats:

```clojure
(def board (g/grid 4 4 (range 16)))

(g/print-table board)
;; =>
;; |  0 |  1 |  2 |  3 |
;; |  4 |  5 |  6 |  7 |
;; |  8 |  9 | 10 | 11 |
;; | 12 | 13 | 14 | 15 |

(g/print-table board :style :single)
;; =>
;; ┌────┬────┬────┬────┐
;; │  0 │  1 │  2 │  3 │
;; ├────┼────┼────┼────┤
;; │  4 │  5 │  6 │  7 │
;; ├────┼────┼────┼────┤
;; │  8 │  9 │ 10 │ 11 │
;; ├────┼────┼────┼────┤
;; │ 12 │ 13 │ 14 │ 15 │
;; └────┴────┴────┴────┘

(g/print-table board :style :double :pad 3 :align :left)
;; =>
;; ╔════════╦════════╦════════╦════════╗
;; ║   0    ║   1    ║   2    ║   3    ║
;; ╠════════╬════════╬════════╬════════╣
;; ║   4    ║   5    ║   6    ║   7    ║
;; ╠════════╬════════╬════════╬════════╣
;; ║   8    ║   9    ║   10   ║   11   ║
;; ╠════════╬════════╬════════╬════════╣
;; ║   12   ║   13   ║   14   ║   15   ║
;; ╚════════╩════════╩════════╩════════╝
```

This is the same functino that is used to display grids in the REPL.
You can set the default options to change how they are shown:

```clojure
(def board (g/grid 4 4 (range 16)))

board
;; =>
;; |  0 |  1 |  2 |  3 |
;; |  4 |  5 |  6 |  7 |
;; |  8 |  9 | 10 | 11 |
;; | 12 | 13 | 14 | 15 |

(g/merge-print-opts! :style :single)

board
;; =>
;; ┌────┬────┬────┬────┐
;; │  0 │  1 │  2 │  3 │
;; ├────┼────┼────┼────┤
;; │  4 │  5 │  6 │  7 │
;; ├────┼────┼────┼────┤
;; │  8 │  9 │ 10 │ 11 │
;; ├────┼────┼────┼────┤
;; │ 12 │ 13 │ 14 │ 15 │
;; └────┴────┴────┴────┘
```
