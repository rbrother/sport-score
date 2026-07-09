(ns sports.add-set-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.add-set :refer [update-names update-score point-range]]))

(deftest update-names-test
  (testing "sets the name at the given index"
    (let [state [{:name nil :score nil} {:name nil :score nil}]]
      (is (= "Roope" (:name (get (update-names state 0 "Roope") 0))))
      (is (nil? (:name (get (update-names state 0 "Roope") 1))))))
  (testing "reassigns the other player's name when it collides"
    (let [state [{:name "Roope" :score nil} {:name "Kari" :score nil}]
          result (update-names state 0 "Kari")]
      (is (= "Kari" (:name (get result 0))))
      ;; The other slot must no longer hold "Kari"; sports.calculations/players
      ;; is ["Roope" "Kari" "Niklas"], so the remaining choice sorted first is "Niklas"
      (is (= "Niklas" (:name (get result 1)))))))

(deftest update-score-test
  (testing "sets the score at the given index"
    (let [state [{:name "Roope" :score nil} {:name "Kari" :score nil}]
          result (update-score state 1 10)]
      (is (= 10 (:score (get result 1))))
      (is (nil? (:score (get result 0))))))
  (testing "also derives the loser's score when setting the winner's score (index 0)"
    (let [state [{:name "Roope" :score nil} {:name "Kari" :score nil}]
          result (update-score state 0 15)]
      (is (= 15 (:score (get result 0))))
      (is (= 13 (:score (get result 1)))))))

(deftest point-range-test
  (testing "winner (index 0) can score between 11 and 19"
    (is (= (range 11 20) (point-range 0 [{} {}]))))
  (testing "loser (index 1) has no options until the winner has a score"
    (is (= [] (point-range 1 [{:score nil} {}]))))
  (testing "loser can score 0-9 when the winner won 11-x"
    (is (= (range 0 10) (point-range 1 [{:score 11} {}]))))
  (testing "loser's score is fixed 2 points below the winner's when winner scored above 11"
    (is (= [13] (point-range 1 [{:score 15} {}])))))
