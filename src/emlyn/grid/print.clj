(ns emlyn.grid.print
  "Functions for printing Grids."
  {:no-doc true}
  (:require [emlyn.grid.impl :as g]
            [emlyn.grid.convert :refer [to-vecs]]
            [emlyn.grid.everywhere :refer [everywhere]]
            [clojure.pprint]
            [clojure.java.io :as io])
  (:import [emlyn.grid.impl Grid]))

(def ^:dynamic *print-opts*
  {:style :markdown
   :pad 1
   :align :right
   :truncate true
   :max-cols 10
   :max-rows 20
   :overflow-val "..."})

(defmacro with-print-opts
  "Temporarily set the default options for printing tables. Options are:
   - `:style` - the style of table to print (default `:markdown`)
   - `:pad` - padding around each cell (default 1)
   - `:align` - alignment of cell values, one of `:left`, `:right`, or `:centre` (default `:right`)
   - `:truncate` - whether to truncate the table to a maximum number of rows and columns (default true)
   - `:max-cols` - maximum number of columns in the table (default 8)
   - `:max-rows` - maximum number of rows in the table (default 16)
   - `:overflow-val` - value to use for cells that overflow the grid (default \"...\")"
  [opts & body]
  `(binding [*print-opts* (merge *print-opts* ~opts)]
     ~@body))

(defn merge-print-opts!
  "Permanently merge the given options with the current print options."
  [& {:as opts}]
  (alter-var-root #'*print-opts* #(merge % opts)))

(def table-styles
  {:vec
   (fn [g] (vec (g/rows g)))
   :tight
   {}
   :space
   {[2 1] " "}
   :simple
   {[2 1] "|"}
   :markdown
   {1 ["|" nil "|" "|"]}
   :single
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
  [val & {:keys [raw]}]
  (count (if raw (pr-str val) (str val))))

(defn write-row
  "Print a row of the table given a sequence of formatted cell values."
  [[leader _ divider trailer] vals nl writer]
  (.write writer
          (str (when nl \newline)
               leader
               (apply str (interpose divider vals))
               trailer))
  true)

(defn write-border
  "Print a horizontal border of the table if there is one."
  [[_ line-char :as seps] widths nl {:keys [pad writer] :or {pad 0}}]
  (when (some identity seps)
    (write-row seps
               (map #(apply str (repeat (+ pad % pad) line-char))
                    widths)
               nl
               writer)))

(defn truncate-grid
  "Truncate a grid to a maximum number of rows and columns, replacing
   overflow values with `overflow-val`."
  [grid max-cols max-rows overflow-val]
  (let [ncols (g/width grid)
        nrows (g/height grid)]
    (case [(boolean (and max-cols (pos? max-cols) (> ncols max-cols)))
           (boolean (and max-rows (pos? max-rows) (> nrows max-rows)))]
      [false false] grid
      [false true] (assoc (g/grid ncols max-rows grid)
                          [[] (dec max-rows)] (everywhere overflow-val))
      [true false] (assoc (g/grid max-cols nrows grid)
                          [(dec max-cols) []] (everywhere overflow-val))
      [true true] (-> (g/grid max-cols max-rows grid)
                      (assoc [[] (dec max-rows)] (everywhere overflow-val))
                      (assoc [(dec max-cols) []] (everywhere overflow-val))))))

(defn write-table
  [grid & {:as opts}]
  (let [{:keys [style truncate max-cols max-rows overflow-val writer]
         :as opts} (merge *print-opts* opts)
        grid (if truncate (truncate-grid grid max-cols max-rows overflow-val) grid)
        style (get table-styles style style)
        style (or style (table-styles :basic))]
    (if (fn? style)
      (print-method (style grid) writer)
      (let [widths (map (fn [col]
                          (apply max 0 (map #(cell-width % opts)
                                            col)))
                        (g/cols grid))
            seps (g/grid 4 4 style)
            started (write-border (seps [[] 0]) widths false opts)]
        (when (pos? (g/height grid))
          (loop [[row & more] (g/rows grid)
                 first? true]
            (when-not first?
              (write-border (seps [[] 2]) widths true opts))
            (write-row (seps [[] 1]) (map #(format-cell %1 %2 opts) row widths) (or started (not first?)) writer)
            (when more
              (recur more false))))
        (write-border (seps [[] 3]) widths true opts)))))

(defn print-table
  "Print a grid as a table.

   ```
   (print-table grid :style :rounded)
   ```"
  [grid & {:keys [writer]
           :or {writer *out*}
           :as opts}]
  (write-table grid :writer writer opts)
  (.write writer "\n"))

(defn table-str
  "Return a string containing a grid formatted as a table."
  [grid & {:as opts}]
  (with-open [sw (java.io.StringWriter.)
              w (io/writer sw)]
    (write-table grid :writer w opts)
    (.flush w)
    (str sw)))

(defmethod print-method Grid [grid writer]
  ;; At the REPL print grids as tables
  (if *print-readably*
    (write-table grid :writer writer)
    (.write writer (str grid))))

(defmethod print-dup Grid [grid writer]
  ;; Prepend the reader macro so it reads back as the correct type:
  (.write writer (binding [*print-dup* false] (str "#emlyn/grid " grid))))

(defn pprint-grid
  [grid]
  (write-table grid :writer *out*))

(. clojure.pprint/simple-dispatch addMethod Grid pprint-grid)
