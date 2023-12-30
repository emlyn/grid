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
      (is (= (g/grid [[0 0 0 0]
                      [0 1 2 3]
                      [0 2 4 6]
                      [0 3 6 9]])
             g))))
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
      (is (nil? (g [3 0])))
      (let [g2 #emlyn/grid "abc\ndef\nghi\njkl"]
        (is (= g g2)))))
  (testing "Construct from vectors"
    (let [g (g/grid [[1 2] [3 4] [5 6]])]
      (is (= 2 (g/width g)))
      (is (= 3 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= (range 1 7) (vals g)))
      (let [g2 #emlyn/grid [[1 2] [3 4] [5 6]]]
        (is (= g g2)))))
  (testing "Construct from map"
    (let [g (g/grid {[0 0] 1
                     [1 1] 2
                     [2 0] 3
                     [2 1] 4})]
      (is (= 3 (g/width g)))
      (is (= 2 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= #emlyn/grid [[1 nil 3]
                          [nil 2 4]]
             g))
      (let [g2 #emlyn/grid {[0 0] 1
                            [1 1] 2
                            [2 0] 3
                            [2 1] 4}]
        (is (= g g2)))))
  (testing "Construct from map of maps"
    (let [g (g/grid 3 2 {0 {0 1}
                         1 {1 2}
                         2 {0 3
                            1 4}})]
      (is (= 3 (g/width g)))
      (is (= 2 (g/height g)))
      (is (= 1 (g [0 0])))
      (is (= #emlyn/grid [[1 nil 3]
                          [nil 2 4]]
             g))
      (let [g2 #emlyn/grid {0 {0 1}
                            1 {1 2}
                            2 {0 3
                               1 4}}]
        (is (= g g2))))))

(deftest construct-error-test
  (is (thrown? IllegalArgumentException (g/grid "123\n45")))
  (is (thrown? IllegalArgumentException (g/grid [1 2 3 4])))
  (is (thrown? IllegalArgumentException (g/grid 2 3 [1 2 3 4])))
  (is (thrown? IllegalArgumentException (g/grid {[1 -1] 3})))
  (is (thrown? IllegalArgumentException (g/grid {1 {-1 3}}))))

(deftest assoc-test
  (let [g #emlyn/grid [[1 2 3]
                       [4 5 6]]]
    (is (= [1 2 3 4 5 6] (vals g)))
    (let [g (assoc g [1 1] 9)]
      (is (= 9 (g [1 1])))
      (is (= #emlyn/grid [[1 2 3]
                          [4 9 6]] g))
      (let [g (dissoc g [0 1])]
        (is (nil? (g [0 1])))
        (is (= #emlyn/grid [[1 2 3]
                            [nil 9 6]] g))))))

(deftest convert-test
  (let [g #emlyn/grid [[1 2 nil] [4 nil 6]]]
    (is (= [1 2 nil
            4 nil 6] (vals g)))
    (is (= [[1 2 nil]
            [4 nil 6]] (g/to-rows g)))
    (is (= {[0 0] 1
            [1 0] 2
            [0 1] 4
            [2 1] 6} (g/to-map g)))
    (is (= {0 {0 1
               1 4}
            1 {0 2}
            2 {1 6}} (g/to-map-of-maps g)))))

(deftest add-rows-test
  (let [g #emlyn/grid [[1 2 3]
                       [4 5 6]]]
    (is (= #emlyn/grid [[1 2 3]
                        [4 5 6]
                        [nil nil nil]]
           (g/add-rows g)))
    (is (= #emlyn/grid [[1 2 3]
                        [4 5 6]
                        [nil nil nil]
                        [nil nil nil]]
           (g/add-rows g :num 2)))
    (is (= #emlyn/grid [[nil nil nil]
                        [1 2 3]
                        [4 5 6]]
           (g/add-rows g :pos 0)))
    (is (= #emlyn/grid [[1 2 3]
                        [nil nil nil]
                        [nil nil nil]
                        [4 5 6]]
           (g/add-rows g :pos 1 :num 2)))))

(deftest add-cols-test
  (let [g #emlyn/grid [[1 2]
                       [3 4]
                       [5 6]]]
    (is (= #emlyn/grid [[1 2 nil]
                        [3 4 nil]
                        [5 6 nil]]
           (g/add-cols g)))
    (is (= #emlyn/grid [[1 2 nil nil]
                        [3 4 nil nil]
                        [5 6 nil nil]]
           (g/add-cols g :num 2)))
    (is (= #emlyn/grid [[nil 1 2]
                        [nil 3 4]
                        [nil 5 6]]
           (g/add-cols g :pos 0)))
    (is (= #emlyn/grid [[1 nil nil 2]
                        [3 nil nil 4]
                        [5 nil nil 6]]
           (g/add-cols g :pos 1 :num 2)))))

(deftest drop-rows-test
  (let [g #emlyn/grid [[1 2]
                       [3 4]
                       [5 6]]]
    (is (= #emlyn/grid [[1 2]
                        [3 4]]
           (g/drop-rows g)))
    (is (= #emlyn/grid [[1 2]]
           (g/drop-rows g :num 2)))
    (is (= #emlyn/grid [[3 4]
                        [5 6]]
           (g/drop-rows g :pos 0)))
    (is (= #emlyn/grid [[5 6]]
           (g/drop-rows g :pos 0 :num 2)))))

(deftest drop-cols-test
  (let [g #emlyn/grid [[1 2 3]
                       [4 5 6]]]
    (is (= #emlyn/grid [[1 2]
                        [4 5]]
           (g/drop-cols g)))
    (is (= #emlyn/grid [[1]
                        [4]]
           (g/drop-cols g :num 2)))
    (is (= #emlyn/grid [[2 3]
                        [5 6]]
           (g/drop-cols g :pos 0)))
    (is (= #emlyn/grid [[3]
                        [6]]
           (g/drop-cols g :pos 0 :num 2)))))

(deftest map-vals-test
  (let [g1 #emlyn/grid [[1 3 5]
                        [2 4 6]]
        g2 (g/map-vals inc g1)]
    (is (= #emlyn/grid [[2 4 6]
                        [3 5 7]]
           g2))
    (let [g3 (g/map-vals + g1 g2)]
      (is (= #emlyn/grid [[3 7 11]
                          [5 9 13]]
             g3)))))

(deftest map-kv-test
  (let [g1 #emlyn/grid [[9 7]
                        [5 3]]
        g2 (g/map-kv (fn [[x y] v] (format "%s,%s:%s" x y v)) g1)]
    (is (= #emlyn/grid [["0,0:9" "1,0:7"]
                        ["0,1:5" "1,1:3"]]
           g2))))

(deftest transpose-test
  (let [g0 #emlyn/grid [[1 2]
                        [3 4]
                        [5 6]]
        g1 (g/transpose g0)]
    (is (= #emlyn/grid [[1 3 5]
                        [2 4 6]] g1))
    (is (= g0 (g/transpose g1)))))

(deftest rotate-test
  (let [g0 #emlyn/grid [[1 2 3]
                        [4 5 6]]
        g90 (g/rotate-right g0)
        g180 (g/rotate-180 g0)
        g270 (g/rotate-left g0)]
    (is (= #emlyn/grid [[4 1]
                        [5 2]
                        [6 3]]
           g90))
    (is (= #emlyn/grid [[6 5 4]
                        [3 2 1]]
           g180))
    (is (= #emlyn/grid [[3 6]
                        [2 5]
                        [1 4]]
           g270))
    (is (= g0 (g/rotate-left g90)))
    (is (= g0 (g/rotate-right g270)))
    (is (= g180 (g/rotate-right g90)))
    (is (= g180 (g/rotate-left g270)))))

(deftest flip-test
  (let [g0 #emlyn/grid [[1 2 3]
                        [4 5 6]]
        glr (g/flip-lr g0)
        gtb (g/flip-tb g0)]
    (is (= #emlyn/grid [[1 2 3]
                        [4 5 6]]
           g0))
    (is (= #emlyn/grid [[3 2 1]
                        [6 5 4]]
           glr))
    (is (= #emlyn/grid [[4 5 6]
                        [1 2 3]]
           gtb))
    (is (= g0 (g/flip-lr glr)))
    (is (= g0 (g/flip-tb gtb)))))

(deftest concat-test
  (testing "Concatenate left-right"
    (let [g0 #emlyn/grid [[1 2 3]
                          [4 5 6]]
          g1 #emlyn/grid [[10]
                          [11]]
          g2 #emlyn/grid [[7 8]
                          [9 0]]]
      (is (= #emlyn/grid [[1 2 3 10 7 8]
                          [4 5 6 11 9 0]]
             (g/concat-lr g0 g1 g2)))))
  (testing "Concatenate top-bottom"
    (let [g0 #emlyn/grid [[1 2]
                          [3 4]
                          [5 6]]
          g1 #emlyn/grid [[10 11]]
          g2 #emlyn/grid [[7 8]
                          [9 0]]]
      (is (= #emlyn/grid [[1 2]
                          [3 4]
                          [5 6]
                          [10 11]
                          [7 8]
                          [9 0]]
             (g/concat-tb g0 g1 g2))))))
