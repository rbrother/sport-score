(ns sports.import-test
  (:require [clojure.string :as str]
            [cljs.test :refer-macros [deftest is testing]]
            [sports.import :refer [pre-sets import-csv-session year-data-from-csv]]))

(deftest pre-sets-test
  (testing "generates pre-match sets for Niklas and Kari wins"
    (let [result (pre-sets ["2" "1"])]
      (is (= 3 (count result)))
      (is (= [{:name "Niklas" :score 11} {:name "Kari"}] (nth result 0)))
      (is (= [{:name "Niklas" :score 11} {:name "Kari"}] (nth result 1)))
      (is (= [{:name "Kari" :score 11} {:name "Niklas"}] (nth result 2)))))
  (testing "generates no sets when both counts are 0"
    (is (= [] (pre-sets ["0" "0"])))))

(defn- csv-row [{:keys [date niklas-pre kari-pre roope-score niklas-score kari-score]}]
  (let [vals (-> (vec (repeat 16 ""))
                 (assoc 2 date)
                 (into [niklas-pre kari-pre ""])
                 ;; the last (ignored) column must be non-blank, otherwise
                 ;; clojure.string/split drops the trailing empty field
                 (into [roope-score niklas-score kari-score "x"]))]
    (str/join "," vals)))

(deftest import-csv-session-test
  (testing "parses a single CSV row into a [date sets] pair"
    (let [row (csv-row {:date "2021-01-01" :niklas-pre "2" :kari-pre "1"
                        :roope-score "21" :niklas-score "" :kari-score "15"})
          [date sets] (import-csv-session row)]
      (is (= :2021-01-01 date))
      (testing "includes the pre-match sets first"
        (is (= [{:name "Niklas" :score 11} {:name "Kari"}] (nth sets 0)))
        (is (= [{:name "Niklas" :score 11} {:name "Kari"}] (nth sets 1)))
        (is (= [{:name "Kari" :score 11} {:name "Niklas"}] (nth sets 2))))
      (testing "includes the scored match, with the winner listed first"
        (is (= [{:name "Roope" :score 21} {:name "Kari" :score 15}] (nth sets 3)))))))

(deftest year-data-from-csv-test
  (testing "parses multiple CSV rows into a map keyed by date"
    (let [row1 (csv-row {:date "2021-01-01" :niklas-pre "0" :kari-pre "0"
                        :roope-score "21" :niklas-score "" :kari-score "15"})
          row2 (csv-row {:date "2021-02-01" :niklas-pre "0" :kari-pre "0"
                        :roope-score "" :niklas-score "21" :kari-score "15"})
          csv (str/join "\n" [row1 row2])
          result (year-data-from-csv csv)]
      (is (= #{:2021-01-01 :2021-02-01} (set (keys result))))
      (is (= [{:name "Roope" :score 21} {:name "Kari" :score 15}] (first (:2021-01-01 result))))
      (is (= [{:name "Niklas" :score 21} {:name "Kari" :score 15}] (first (:2021-02-01 result)))))))
