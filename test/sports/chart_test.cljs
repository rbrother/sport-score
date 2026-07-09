(ns sports.chart-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.chart :refer [prepare-chart-data]]))

(def cumulative-data
  [{:name "Roope"
    :data [{:date "2024-01-01" :points 1 :cumulative 1}
           {:date "2024-02-01" :points 0.5 :cumulative 1.5}]}
   {:name "Kari"
    :data [{:date "2024-01-01" :points 0 :cumulative 0}]}])

(deftest prepare-chart-data-test
  (testing "collects the distinct, sorted union of all dates as labels"
    (is (= ["2024-01-01" "2024-02-01"] (:labels (prepare-chart-data cumulative-data)))))
  (testing "builds one dataset per player with 0 filled in for missing dates"
    (let [datasets (:datasets (prepare-chart-data cumulative-data))
          roope (first (filter #(= "Roope" (:label %)) datasets))
          kari (first (filter #(= "Kari" (:label %)) datasets))]
      (is (= [1 1.5] (:data roope)))
      (is (= [0 0] (:data kari)))))
  (testing "uses a known color for recognised players, and a default for others"
    (let [datasets (:datasets (prepare-chart-data
                                (conj cumulative-data {:name "Someone Else" :data []})))
          roope (first (filter #(= "Roope" (:label %)) datasets))
          other (first (filter #(= "Someone Else" (:label %)) datasets))]
      (is (= "#FF6384" (:borderColor roope)))
      (is (= "#999999" (:borderColor other))))))
