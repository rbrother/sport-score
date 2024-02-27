(ns sports.years
  (:require [clojure.string :as s]
            [cljs.pprint :refer [pprint]]
            [re-frame.core :as rf]
            [sports.calculations :as calc]
            [sports.year :as year-view]))

(defn view []
  (let [player-cols (s/join " " (repeat (count calc/players) "80px"))
        years @(rf/subscribe [:years])]
    [:div.grid {:style {:grid-template-columns (str "100px auto " player-cols " 1fr")}}
     [:div.bold "Year"] [:div.bold "Sessions"]
     (into [:<>] (for [p calc/players] [:div.bold p])) [:div]
     [:div.row-line]
     (into [:<>]
           (for [year (reverse (sort (keys years)))]
             (let [on-click #(rf/dispatch [:show-year year])
                   {:keys [sessions players]} @(rf/subscribe [:year-summary year])]
               [:<>
                [:div.link {:on-click on-click} year]
                [:div (count sessions)]
                [year-view/player-points-cells players true?]
                [:div]])))]))

(rf/reg-event-db :show-year
  (fn [db [_ year]]
    (assoc db :navigation
              {:page :year
               :year year})))