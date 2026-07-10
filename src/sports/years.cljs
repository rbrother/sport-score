(ns sports.years
  (:require [clojure.string :as s]
            [cljs.pprint :refer [pprint]]
            [re-frame.core :as rf]
            [sports.calculations :as calc]
            [sports.routes :as routes]
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
             (let [on-click #(routes/navigate! (routes/year-url year))
                   {:keys [sessions players]} @(rf/subscribe [:year-summary year])]
               [:<>
                [:div.link {:on-click on-click} year]
                [:div (count sessions)]
                [year-view/player-points-cells players true?]
                [:div]])))]))