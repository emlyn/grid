(ns emlyn.regression-test
  (:require [clojure.test :refer [deftest is]]))

(deftest single-cell-grid
  (let [g #emlyn/grid [[1]]]
    (is (= 1 (g [0 0])))))
