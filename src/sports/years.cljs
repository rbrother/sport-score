(ns sports.years
  (:require [clojure.string :as s]
            [cljs.pprint :refer [pprint]]
            [re-frame.core :as rf]
            [sports.calculations :as calc]
            [sports.routes :as routes]
            [sports.widgets :as widgets]
            [sports.year :as year-view]))

(defn view []
  (let [player-cols (s/join " " (repeat (count calc/players) "56px"))
        years @(rf/subscribe [:years])]
    [:div
     [widgets/header {:title "🏓 Sport Tracker"}]
     [:div.page
      [:div.card
       [:div.table-scroll
        [:div.grid {:style {:grid-template-columns (str "52px 60px " player-cols)}}
         [:div.col-header "Year"] [:div.col-header "Sessions"]
         (into [:<>] (for [p calc/players] [:div.col-header p]))
         [:div.row-line]
         (into [:<>]
               (for [year (reverse (sort (keys years)))]
                 (let [on-click #(routes/navigate! (routes/year-url year))
                       {:keys [sessions players]} @(rf/subscribe [:year-summary year])]
                   [:<>
                    [:div.link {:on-click on-click} year]
                    [:div (count sessions)]
                    [year-view/player-points-cells players true?]])))]]]]]))