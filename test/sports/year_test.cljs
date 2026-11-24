(ns sports.year-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [sports.year :refer [mark-ignored preserve-imported-payments]]))

(deftest mark-ignored-test
  (testing "wraps the value in parentheses when ignored"
    (is (= "(5.0)" (mark-ignored "5.0" true))))
  (testing "leaves the value untouched when not ignored"
    (is (= "5.0" (mark-ignored "5.0" false)))))

(deftest import-preserves-payments-test
  (let [old {:2026-01-01 {:sets [] :payment {:cost-cents 1200 :payer "Roope"}}}
        sets [[{:name "Roope"} {:name "Kari"}]]]
    (is (= {:2026-01-01 {:sets sets :payment {:cost-cents 1200 :payer "Roope"}}}
           (preserve-imported-payments old {:2026-01-01 sets})))
    (is (nil? (preserve-imported-payments old {})))))
