(ns responder.core-test
  (:require [clojure.test :refer :all]
            [responder.main :refer :all]))

(deftest howdy
  (testing "Something that could be true"
    (is (= 1 (+ 0 1)))))
