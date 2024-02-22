(ns sports.subs
  (:require [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]
            [sports.log :as log]))

(rf/reg-sub :status (fn [db _] (:status db)))

(rf/reg-sub :navigation (fn [db _] (:navigation db)))

(rf/reg-sub :page :<- [:navigation] (fn [nav _] (:page nav)))

(rf/reg-sub :selected-year :<- [:navigation] (fn [nav _] (:year nav)))

(rf/reg-sub :selected-session :<- [:navigation] (fn [nav _] (:session nav)))

(rf/reg-sub :years (fn [db _] (:years db)))

(rf/reg-sub :year-data :<- [:years] :<- [:selected-year]
  (fn [[years selected] _] (get years selected)))

(rf/reg-sub :session-data :<- [:year-data] :<- [:selected-session]
  (fn [[year-data session-id]] (get year-data session-id)))

(rf/reg-sub :players (fn [db _] ["Roope", "Kari", "Niklas"]))

