(ns sports.payments-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.calculations :as calc]
            [sports.payments :as payments]))

(def two-player-sets [[{:name "Roope"} {:name "Kari"}]])
(def three-player-sets (conj two-player-sets [{:name "Niklas"} {:name "Kari"}]))

(deftest format-euros-test
  (testing "rounds to the nearest euro, with halves away from zero"
    (is (= ["€0" "€0" "€1" "€1" "€2"]
           (mapv payments/format-euros [0 49 50 149 150])))
    (is (= ["€0" "-€1" "-€1" "-€2"]
           (mapv payments/format-euros [-49 -50 -149 -150])))))

(deftest suggested-payer-test
  (testing "uses exact balances and breaks ties in player order"
    (is (= "Roope" (payments/suggested-payer {"Roope" 0 "Kari" 0 "Niklas" 0} two-player-sets)))
    (is (= "Kari" (payments/suggested-payer {"Roope" -100 "Kari" -101 "Niklas" 201} two-player-sets)))
    (is (= "Kari" (payments/suggested-payer {"Roope" 300 "Kari" 100 "Niklas" 200} two-player-sets))))
  (testing "removes this session's payment credit but retains its cost shares"
    (let [session {:sets two-player-sets :payment {:cost-cents 1200 :payer "Roope"}}
          year-data {:2026 {:2026-01-01 session}}
          balances (:balances (payments/balances year-data :2026))]
      (is (= "Roope" (payments/suggested-payer balances session)))))
  (testing "does not subtract credits for payments excluded from balances"
    (let [session {:sets [] :payment {:cost-cents 1200 :payer "Niklas"}}]
      (is (= "Roope" (payments/suggested-payer {"Roope" 0 "Kari" 0 "Niklas" 0} session))))))

(deftest payment-validation-test
  (testing "amounts must be non-negative whole cents within the supported range"
    (is (= 1234 (payments/parse-cost "12,34")))
    (is (= 1200 (payments/parse-cost "12")))
    (is (nil? (payments/parse-cost "12.345")))
    (is (nil? (payments/parse-cost "-1")))
    (is (nil? (payments/parse-cost "1000000.01"))))
  (testing "nonzero payments need a payer and recorded participants"
    (is (some? (payments/payment-error :2026-01-01 [] "10" "Roope")))
    (is (some? (payments/payment-error :2026-01-01 two-player-sets "10" "")))
    (is (nil? (payments/payment-error :2026-01-01 two-player-sets "10" "Niklas")))
    (is (nil? (payments/payment-error :2026-01-01 [] "" "")))
    (is (some? (payments/payment-error :2025-01-01 two-player-sets "10" "Roope")))))

(deftest balanced-shares-test
  (doseq [sets [two-player-sets three-player-sets]
          cents [1 2 100 1001]]
    (let [result (payments/shares :2026-02-01 sets cents)
          values (vals result)]
      (is (= cents (apply + values)))
      (is (<= (- (apply max values) (apply min values)) 1))
      (is (= result (payments/shares :2026-02-01 sets cents)))))
  (is (not= (payments/shares :2026-02-01 three-player-sets 1)
            (payments/shares :2026-02-02 three-player-sets 1))))

(deftest yearly-balances-test
  (let [paid (fn [sets cents payer] {:sets sets :payment {:cost-cents cents :payer payer}})
        years {:2025 {:2025-12-31 (paid two-player-sets 900 "Roope")}
               :2026 {:2026-01-01 (paid two-player-sets 1200 "Niklas")
                      :2026-01-02 two-player-sets
                      :2026-01-03 (paid [] 100 "Kari")}
               :2027 {:2027-01-01 (paid three-player-sets 900 "Roope")
                      :2027-01-02 (paid [] 100 "Kari")}}
        result-2026 (payments/balances years :2026)
        result-2027 (payments/balances years :2027)
        result-2028 (payments/balances years :2028)]
    (is (= {"Roope" -600 "Kari" -600 "Niklas" 1200} (:balances result-2026)))
    (is (= [{:year :2026 :date :2026-01-03}] (:pending result-2026)))
    (is (= {"Roope" 600 "Kari" -300 "Niklas" -300} (:balances result-2027)))
    (is (= [{:year :2027 :date :2027-01-02}] (:pending result-2027)))
    (is (= {"Roope" 0 "Kari" 0 "Niklas" 0} (:balances result-2028)))
    (is (empty? (:pending result-2028)))
    (is (zero? (apply + (vals (:balances result-2027)))))
    (testing "calendar date determines the balance year even if stored under another year"
      (is (= {"Roope" -600 "Kari" -600 "Niklas" 1200}
             (:balances (payments/balances
                          {:2026 {:2027-01-01 (paid two-player-sets 1200 "Niklas")}}
                          :2027)))))))

(deftest sets-preserve-payment-test
  (let [session {:sets two-player-sets :payment {:cost-cents 1200 :payer "Niklas"}}
        edited (calc/replace-session-sets session three-player-sets)]
    (is (= (:payment session) (:payment edited)))
    (is (= three-player-sets (:sets (calc/analyze-session edited))))
    (is (= three-player-sets (calc/replace-session-sets two-player-sets three-player-sets)))))
