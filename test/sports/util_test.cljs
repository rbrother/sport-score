(ns sports.util-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.util :refer [attr= index-by-id shorten starts-with try-parse-int]]))

(deftest attr=-test
  (testing "returns a predicate matching a specific attribute value"
    (let [p1? (attr= :name "Roope")]
      (is (true? (p1? {:name "Roope" :score 21})))
      (is (false? (p1? {:name "Kari" :score 21})))
      (is (false? (p1? {}))))))

(deftest index-by-id-test
  (testing "indexes a list of maps by their :id"
    (is (= {1 {:id 1 :name "a"} 2 {:id 2 :name "b"}}
           (index-by-id [{:id 1 :name "a"} {:id 2 :name "b"}]))))
  (testing "returns an empty map for an empty list"
    (is (= {} (index-by-id [])))))

(deftest shorten-test
  (testing "leaves short strings untouched"
    (is (= "short string" (shorten "short string"))))
  (testing "truncates long strings and appends an ellipsis"
    (let [long-string "this is a much longer string than the max length"]
      (is (= (str (subs long-string 0 25) "...") (shorten long-string))))))

(deftest starts-with-test
  (testing "detects a matching prefix"
    (is (true? (starts-with "abc" "abcdef"))))
  (testing "detects a non-matching prefix"
    (is (false? (starts-with "abc" "xyz"))))
  (testing "returns nil (falsy) when the string is nil"
    (is (nil? (starts-with "abc" nil)))))

(deftest try-parse-int-test
  (testing "parses valid integer strings"
    (is (= 42 (try-parse-int "42"))))
  (testing "returns nil for an empty string"
    (is (nil? (try-parse-int ""))))
  (testing "returns the original string when it isn't a number"
    (is (= "abc" (try-parse-int "abc")))))
