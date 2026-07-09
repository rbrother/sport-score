(ns sports.calculations-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.calculations :refer [count-victories sum-of
                                          collect-player-victories
                                          analyze-session
                                          cumulative-scores-over-time
                                          year-summary]]))

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

(deftest collect-player-victories-test
  (testing "awards 1 point for a plain net-victory"
    (let [sets [[{:name "Roope" :score 21} {:name "Kari" :score 15}]
                [{:name "Roope" :score 21} {:name "Kari" :score 18}]]
          [roope-vs-kari] (collect-player-victories sets "Roope" ["Roope" "Kari"])
          [kari-vs-roope] (collect-player-victories sets "Kari" ["Roope" "Kari"])]
      (is (= {:opponent "Kari" :victories 2 :victories-main 2
              :losses 0 :losses-main 0
              :net-victories 2 :net-victories-main 2 :points 1}
             roope-vs-kari))
      (is (= {:opponent "Roope" :victories 0 :victories-main 0
              :losses 2 :losses-main 2
              :net-victories -2 :net-victories-main -2 :points 0}
             kari-vs-roope))))
  (testing "awards 0.5 points for a draw (net-victories 0)"
    (let [sets [[{:name "Roope" :score 21} {:name "Kari" :score 15}]
                [{:name "Kari" :score 21} {:name "Roope" :score 18}]]
          [roope-vs-kari] (collect-player-victories sets "Roope" ["Roope" "Kari"])]
      (is (= 0 (:net-victories roope-vs-kari)))
      (is (= 0.5 (:points roope-vs-kari)))))
  (testing "awards 1.5 points when both total and main net-victories reach 3"
    (let [sets (repeat 4 [{:name "Roope" :score 21} {:name "Kari" :score 15}])
          [roope-vs-kari] (collect-player-victories sets "Roope" ["Roope" "Kari"])]
      (is (= 4 (:net-victories roope-vs-kari)))
      (is (= 1.5 (:points roope-vs-kari))))))

(deftest analyze-session-test
  (testing "marks a session as counted only when all core players are present"
    (let [two-player-sets [[{:name "Roope" :score 21} {:name "Kari" :score 15}]]
          all-player-sets [[{:name "Roope" :score 21} {:name "Kari" :score 15}]
                           [{:name "Niklas" :score 21} {:name "Kari" :score 15}]
                           [{:name "Roope" :score 21} {:name "Niklas" :score 15}]]]
      (is (false? (:include-in-year-points? (analyze-session two-player-sets))))
      (is (true? (:include-in-year-points? (analyze-session all-player-sets))))))
  (testing "aggregates per-player victories, losses and points"
    (let [sets [[{:name "Roope" :score 21} {:name "Kari" :score 15}]
                [{:name "Roope" :score 21} {:name "Kari" :score 18}]]
          {:keys [players]} (analyze-session sets)
          roope (first (filter #(= "Roope" (:name %)) players))
          kari (first (filter #(= "Kari" (:name %)) players))]
      (is (= 2 (:victories roope)))
      (is (= 0 (:losses roope)))
      (is (= 1 (:points roope)))
      (is (= 0 (:victories kari)))
      (is (= 2 (:losses kari)))
      (is (= 0 (:points kari))))))

(deftest year-summary-test
  (testing "only includes sessions with all core players in the totals"
    (let [full-session [[{:name "Roope" :score 21} {:name "Kari" :score 15}]
                        [{:name "Niklas" :score 21} {:name "Kari" :score 15}]
                        [{:name "Roope" :score 21} {:name "Niklas" :score 15}]]
          partial-session [[{:name "Roope" :score 21} {:name "Kari" :score 15}]]
          year-data {:2024-01-01 full-session
                     :2024-02-01 partial-session}
          {:keys [players]} (year-summary year-data)
          roope (first (filter #(= "Roope" (:name %)) players))]
      ;; Roope wins both his sets in the full session (2 points); the partial
      ;; session (missing Niklas) must not contribute to the total.
      (is (= 2 (:points roope))))))

(deftest cumulative-scores-over-time-test
  (testing "accumulates points per player over sorted, fully-attended sessions"
    (let [session-1 [[{:name "Roope" :score 21} {:name "Kari" :score 15}]
                     [{:name "Niklas" :score 21} {:name "Kari" :score 15}]
                     [{:name "Roope" :score 21} {:name "Niklas" :score 15}]]
          session-2 [[{:name "Kari" :score 21} {:name "Roope" :score 15}]
                     [{:name "Niklas" :score 21} {:name "Roope" :score 15}]
                     [{:name "Kari" :score 21} {:name "Niklas" :score 15}]]
          year-data {:2024-02-01 session-2 :2024-01-01 session-1}
          result (cumulative-scores-over-time year-data)
          roope (first (filter #(= "Roope" (:name %)) result))]
      (testing "sessions are processed in date order regardless of map order"
        (is (= ["2024-01-01" "2024-02-01"] (mapv :date (:data roope)))))
      (testing "cumulative total carries forward across sessions"
        (is (= [2 0] (mapv :points (:data roope))))
        (is (= [2 2] (mapv :cumulative (:data roope))))))))
