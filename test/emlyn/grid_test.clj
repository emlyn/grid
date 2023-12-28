(ns emlyn.grid-test
  (:require [clojure.test :refer [deftest testing is]]
            [emlyn.grid :as g]))

(deftest construct-with-shape-test
  (testing "Construct from just shape args"
    (let [g (g/grid 2 3)]
      (is (= 2 (g/width g)))
      (is (= 3 (g/height g)))
      (is (nil? (g [0 0])))
      (is (= (repeat 6 nil) (vals g)))))
  (testing "Construct from shape and flat vector"
    (let [g (g/grid 2 3 [1 2 3 4 5 6])]
      (is (= 2 (g/width g)))
      (is (= 3 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= (range 1 7) (vals g)))))
  (testing "Construct from shape and function"
    (let [g (g/grid 4 4 *)]
      (is (= [0 0 0 0
              0 1 2 3
              0 2 4 6
              0 3 6 9] (vals g)))))
  (testing "Construct from shape and vectors"
    (let [g (g/grid 2 3 [[1 2] [3 4] [5 6]])]
      (is (= 2 (g/width g)))
      (is (= 3 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= (range 1 7) (vals g)))))
  (testing "Construct from shape and map"
    (let [g (g/grid 3 2 {[0 0] 1
                         [1 1] 2
                         [2 0] 3
                         [2 1] 4})]
      (is (= 3 (g/width g)))
      (is (= 2 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= [ 1 nil 3
              nil 2  4] (vals g)))))
  (testing "Construct from shape and map of maps"
    (let [g (g/grid 3 2 {0 {0 1}
                         1 {1 2}
                         2 {0 3
                            1 4}})]
      (is (= 3 (g/width g)))
      (is (= 2 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= [1 nil 3
              nil 2 4] (vals g))))))

(deftest construct-without-shape-test
  (testing "Construct from string"
    (let [g (g/grid "abc\ndef\nghi\njkl")]
      (is (= 3 (g/width g)))
      (is (= 4 (g/height g)))
      (is (= \a (g [0 0])))
      (is (= \l (g [2 3])))
      (is (nil? (g [3 0])))))
  (testing "Construct from vectors"
    (let [g (g/grid [[1 2] [3 4] [5 6]])]
      (is (= 2 (g/width g)))
      (is (= 3 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= (range 1 7) (vals g)))))
  (testing "Construct from map"
    (let [g (g/grid {[0 0] 1
                     [1 1] 2
                     [2 0] 3
                     [2 1] 4})]
      (is (= 3 (g/width g)))
      (is (= 2 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= [1  nil 3
              nil 2  4] (vals g)))))
  (testing "Construct from map of maps"
    (let [g (g/grid 3 2 {0 {0 1}
                         1 {1 2}
                         2 {0 3
                            1 4}})]
      (is (= 3 (g/width g)))
      (is (= 2 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= [1  nil 3
              nil 2  4] (vals g))))))

(deftest construct-error-test
  (is (thrown? IllegalArgumentException (g/grid "123\n45")))
  (is (thrown? IllegalArgumentException (g/grid [1 2 3 4])))
  (is (thrown? IllegalArgumentException (g/grid 2 3 [1 2 3 4])))
  (is (thrown? IllegalArgumentException (g/grid {[1 -1] 3})))
  (is (thrown? IllegalArgumentException (g/grid {1 {-1 3}}))))

(deftest assoc-test
  (let [g (g/grid 3 2 [[1 2 3] [4 5 6]])]
    (is (= [1 2 3 4 5 6] (vals g)))
    (let [g (assoc g [1 1] 9)]
      (is (= 9 (g [1 1])))
      (is (= [1 2 3 4 9 6] (vals g)))
      (let [g (dissoc g [0 1])]
        (is (nil? (g [0 1])))
        (is (= [1 2 3
                nil 9 6] (vals g)))))))

(deftest map-vals-test
  (let [g1 (g/grid [[1 3 5] [2 4 6]])
        g2 (g/map-vals inc g1)]
    (is (= (g/width g1) (g/width g2)))
    (is (= (g/height g1) (g/height g2)))
    (is (= [2 4 6 3 5 7] (vals g2)))))

(deftest map-kv-test
  (let [g1 (g/grid 2 2 [[9 7] [5 3]])
        g2 (g/map-kv (fn [[x y] v] (format "%s,%s:%s" x y v)) g1)]
    (is (= (g/width g1) (g/width g2)))
    (is (= (g/height g1) (g/height g2)))
    (is (= ["0,0:9" "1,0:7" "0,1:5" "1,1:3"] (vals g2)))))

(deftest transpose-test
  (let [g0 (g/grid 2 3 [[1 2] [3 4] [5 6]])]
    (is (= 2 (g/width g0)))
    (is (= 3 (g/height g0)))
    (is (= [1 2
            3 4
            5 6] (vals g0)))
    (let [g1 (g/transpose g0)]
      (is (= 3 (g/width g1)))
      (is (= 2 (g/height g1)))
      (is (= [1 3 5
              2 4 6] (vals g1)))
      (is (= g0 (g/transpose g1))))))

(deftest rotate-test
  (let [g0 (g/grid 3 2 [1 2 3 4 5 6])
        g90 (g/rotate-right g0)
        g180 (g/rotate-180 g0)
        g270 (g/rotate-left g0)]
    (is (= [1 2 3
            4 5 6] (vals g0)))
    (is (= [4 1
            5 2
            6 3] (vals g90)))
    (is (= [6 5 4
            3 2 1] (vals g180)))
    (is (= [3 6
            2 5
            1 4] (vals g270)))
    (is (= g0 (g/rotate-left g90)))
    (is (= g0 (g/rotate-right g270)))
    (is (= g180 (g/rotate-right g90)))
    (is (= g180 (g/rotate-left g270)))))

(deftest flip-test
  (let [g0 (g/grid 3 2 [1 2 3 4 5 6])
        glr (g/flip-lr g0)
        gtb (g/flip-tb g0)]
    (is (= [1 2 3
            4 5 6] (vals g0)))
    (is (= [3 2 1
            6 5 4] (vals glr)))
    (is (= [4 5 6
            1 2 3] (vals gtb)))
    (is (= g0 (g/flip-lr glr)))
    (is (= g0 (g/flip-tb gtb)))))
