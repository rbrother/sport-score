(ns sports.year-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.year :refer [mark-ignored]]))

(deftest mark-ignored-test
  (testing "wraps the value in parentheses when ignored"
    (is (= "(5.0)" (mark-ignored "5.0" true))))
  (testing "leaves the value untouched when not ignored"
    (is (= "5.0" (mark-ignored "5.0" false)))))
