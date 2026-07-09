(ns sports.session-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.session :refer [hide-zero scores-a-b]]))

(deftest hide-zero-test
  (testing "hides zero values"
    (is (nil? (hide-zero 0))))
  (testing "keeps positive values"
    (is (= 5 (hide-zero 5)))))

(deftest scores-a-b-test
  (testing "looks up a player's match result against a specific opponent"
    (let [players-data [{:name "Roope"
                         :matches [{:opponent "Kari" :points 1}
                                   {:opponent "Niklas" :points 0.5}]}
                        {:name "Kari"
                         :matches [{:opponent "Roope" :points 0}]}]]
      (is (= {:opponent "Kari" :points 1} (scores-a-b players-data "Roope" "Kari")))
      (is (= {:opponent "Roope" :points 0} (scores-a-b players-data "Kari" "Roope")))
      (is (nil? (scores-a-b players-data "Roope" "Unknown"))))))
