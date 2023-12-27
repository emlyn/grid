(ns emlyn.grid-test
  (:require [clojure.test :refer [deftest is]]
            [emlyn.grid :as g]))

(deftest init-empty-test
  (let [g (g/grid 2 3)]
    (is (= 2 (g/width g)))
    (is (= 3 (g/height g)))
    (is (nil? (g [0 0])))
    (let [gg (-> g
                 (assoc [0 0] 1)
                 (assoc [1 0] 2)
                 (assoc [0 1] 3)
                 (assoc [1 1] 4)
                 (assoc [0 2] 5)
                 (assoc [1 2] 6))]
      (is (= 1 (gg [0 0])))
      (is (= 6 (gg [1 2])))
      (is (nil? (gg [2 0])))
      (is (= [1 2 3 4 5 6] (vals gg))))))

(deftest init-string-test
  (let [g (g/grid "abc\ndef\nghi\njkl")]
    (is (= 3 (g/width g)))
    (is (= 4 (g/height g)))
    (is (= \a (g [0 0])))
    (is (= \l (g [2 3])))
    (is (nil? (g [3 0])))))

(deftest init-fn-test
  (let [g (g/grid 3 2 +)]
    (is (= [0 1 2 1 2 3] (vals g)))))

(deftest bad-init-test
  (is (thrown? IllegalArgumentException (g/grid "123\n45")))
  (is (thrown? IllegalArgumentException (g/grid [1 2 3 4]))))

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
