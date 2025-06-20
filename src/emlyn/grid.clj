(ns emlyn.grid
  "The Grid public API."
  (:require [potemkin :refer [import-vars]]
            [emlyn.grid.impl]
            [emlyn.grid.everywhere]
            [emlyn.grid.operations]
            [emlyn.grid.transforms]
            [emlyn.grid.convert]
            [emlyn.grid.print]))

(import-vars
  [emlyn.grid.impl
    grid
    width
    height
    rows
    cols
    with-index-mode
    set-index-mode!]
  [emlyn.grid.everywhere
    everywhere]
  [emlyn.grid.operations
    add-rows
    add-cols
    drop-rows
    drop-cols
    map-vals
    map-kv
    concat-lr
    concat-tb]
  [emlyn.grid.transforms
    transpose
    rotate-right
    rotate-left
    rotate-180
    flip-lr
    flip-tb]
  [emlyn.grid.convert
    to-vec
    to-vecs
    to-map
    to-maps]
  [emlyn.grid.print
    with-print-opts
    merge-print-opts!
    print-table
    table-str])
