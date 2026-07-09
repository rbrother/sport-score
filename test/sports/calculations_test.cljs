(ns sports.calculations-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.calculations :refer [count-victories sum-of]]))

(def sample-sets
  [[{:name "Roope" :score 21} {:name "Kari" :score 15}]
   [{:name "Roope" :score 21} {:name "Kari" :score 18}]
   [{:name "Kari" :score 21} {:name "Roope" :score 19}]
   [{:name "Niklas"} {:name "Kari"}]]) ;; pre-match (no score) between Niklas and Kari

(deftest count-victories-test
  (testing "counts only completed sets by default"
    (is (= 2 (count-victories sample-sets "Roope" "Kari" false)))
    (is (= 1 (count-victories sample-sets "Kari" "Roope" false))))
  (testing "includes pre-matches when requested"
    (is (= 1 (count-victories sample-sets "Niklas" "Kari" true)))
    (is (= 0 (count-victories sample-sets "Niklas" "Kari" false)))))

(deftest sum-of-test
  (testing "sums selected values from a sequence of maps"
    (is (= 6 (sum-of [{:points 1} {:points 2} {:points 3}] :points)))
    (is (= 0 (sum-of [] :points)))))
