(ns sports.subs
  (:require [re-frame.core :as rf]
            [sports.calculations :as calc]))

(rf/reg-sub :status (fn [db _] (:status db)))

(rf/reg-sub :navigation (fn [db _] (:navigation db)))

(rf/reg-sub :page :<- [:navigation] (fn [nav _] (:page nav)))

(rf/reg-sub :selected-year :<- [:navigation] (fn [nav _] (:year nav)))

(rf/reg-sub :selected-session :<- [:navigation] (fn [nav _] (:session nav)))

(rf/reg-sub :years (fn [db _] (:years db)))

(rf/reg-sub :year-data :<- [:years]
  (fn [years [_ year]] (get years year)))

(rf/reg-sub :current-year-data :<- [:years] :<- [:selected-year]
  (fn [[years current] _] (get years current)))

(rf/reg-sub :year-summary
  (fn [[_ year]]
    (rf/subscribe [:year-data year]))
  (fn [year-data _]
    (calc/year-summary year-data)))

;; See https://day8.github.io/re-frame/subscriptions/ for more complex subscription syntax like below
(rf/reg-sub :session-data
  (fn [[_ year _date]]
    (rf/subscribe [:year-data year]))
  (fn [year-data [_ _year date]]
    (calc/analyze-session (get year-data date))))

