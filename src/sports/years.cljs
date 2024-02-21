(ns sports.years
  (:require [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]
            [sports.log :as log]))

(defn view []
  (let [years @(rf/subscribe [:years])]
    [:div
     (for [year (sort (keys years))]
       (let [on-click #(rf/dispatch [::show-year year])]
         ^{:key year} [:div [:button.navigation {:on-click on-click} year]]))]))

(rf/reg-event-db ::show-year
  (fn [db [_ year]]
    (assoc db :navigation
              {:page :year
               :year year})))