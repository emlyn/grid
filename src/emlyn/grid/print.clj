(ns emlyn.grid.print
  (:require [emlyn.grid.impl :as g]
            [emlyn.grid.convert :refer [to-vecs]]
            [clojure.pprint])
  (:import [emlyn.grid.impl Grid]))

(defmethod print-method Grid [grid writer]
  (print-method (to-vecs grid) writer))

(defmethod print-dup Grid [grid writer]
  (.write writer "#emlyn/grid ")
  ;; By default, to-vecs creates subvectors, which print differently,
  ;; so convert them to plain vectors for printing:
  (print-dup (mapv #(into [] %)
                   (to-vecs grid))
             writer))

(defn pprint-grid
  [grid]
  (clojure.pprint/pprint (to-vecs grid)))

(. clojure.pprint/simple-dispatch addMethod Grid pprint-grid)

(def table-styles
  {:tight
   {}
   :space
   {[2 1] " "}
   :simple
   {[2 1] "|"}
   :light
   [["┌" "─" "┬" "┐"]
    ["│" nil "│" "│"]
    ["├" "─" "┼" "┤"]
    ["└" "─" "┴" "┘"]]
   :rounded
   [["╭" "─" "┬" "╮"]
    ["│" nil "│" "│"]
    ["├" "─" "┼" "┤"]
    ["╰" "─" "┴" "╯"]]
   :heavy
   [["┏" "━" "┳" "┓"]
    ["┃" nil "┃" "┃"]
    ["┣" "━" "╋" "┫"]
    ["┗" "━" "┻" "┛"]]
   :double
   [["╔" "═" "╦" "╗"]
    ["║" nil "║" "║"]
    ["╠" "═" "╬" "╣"]
    ["╚" "═" "╩" "╝"]]
   :ascii
   [["/" "-" "+" "\\"]
    ["|" nil "|" "|"]
    ["+" "-" "+" "+"]
    ["\\" "-" "+" "/"]]})

(defn format-cell
  "Format a table cell value as a string with alignment/padding etc."
  [val width & {:keys [pad align raw]
                :or {pad 0 align :right}}]
  (let [valstr (if raw (pr-str val) (str val))
        valstr (if (#{:centre :center} align)
                 (apply str valstr (repeat (quot (- width (count valstr)) 2) " "))
                 valstr)
        padstr (apply str (repeat pad " "))
        fmt (str padstr
                 "%" (when (pos? width)
                       (str (if (= align :left) "-" "")
                            width))
                 "s"
                 padstr)]
    (format fmt valstr)))

(defn cell-width
  "Get the width a value will be as a table cell (without counting padding)."
  [val & {:as opts}]
  (count (format-cell val 0 (assoc opts :pad 0))))

(defn print-row
  "Print a row of the table given a sequence of formatted cell values."
  [[leader _ divider trailer] vals]
  (println (str leader
                (apply str (interpose divider vals))
                trailer)))

(defn print-border
  "Print a horizontal border of the table if there is one."
  [[_ line-char :as seps] widths {:keys [pad] :or {pad 0}}]
  (when (some identity seps)
    (print-row seps (map #(apply str (repeat (+ pad % pad) line-char))
                         widths))))

(defn print-table
  "Print a grid as a table."
  [grid & {:keys [style]
           :or {style :simple}
           :as opts}]
  (let [widths (map (fn [x]
                      (apply max 0 (map #(cell-width % opts)
                                        (grid [x []]))))
                    (range (g/width grid)))
        seps (g/grid 4 4 (get table-styles style style))]
    (print-border (seps [[] 0]) widths opts)
    (dotimes [y (g/height grid)]
      (when (pos? y)
        (print-border (seps [[] 2]) widths opts))
      (print-row (seps [[] 1]) (map #(format-cell %1 %2 opts) (grid [[] y]) widths)))
    (print-border (seps [[] 3]) widths opts)))

(defn table-str
  "Return a string containing a grid formatted as a table."
  [grid & {:as opts}]
  (with-out-str
    (print-table grid opts)))
