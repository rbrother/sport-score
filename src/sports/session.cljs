(ns sports.session
  (:require [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]
            [sports.log :as log]
            [sports.util :as util]))

(defn sets-table [sets]
  (into
    [:div.grid {:style {:grid-template-columns "auto 80px 80px 80px 1fr"}}
     [:div.bold "#"] [:div.bold "Player 1"] [:div.bold "Score"] [:div.bold "Player 2"] [:div]]
    (->> sets
         (map-indexed
           (fn [index [{winner-name :name, winner-score :score}
                       {loser-name :name, loser-score :score}]]
             [:<> [:div (inc index)]
              [:div.winner winner-name]
              [:div
               [:span.winner winner-score] " - "
               [:span.loser loser-score]]
              [:div.loser loser-name]
              [:div]]))
         reverse)))

(defn match-line [name {:keys [opponent victories losses victories-main losses-main
                               net-victories net-victories-main points]}]
  [[:div name]
   [:div opponent]
   [:div.winner victories (when (not= victories-main victories)
                            (str " (" victories-main ")"))]
   [:div.loser losses (when (not= losses-main losses)
                        (str " (" losses-main ")"))]
   [:div {:class (if (> net-victories 0) "winner" "loser")} net-victories]
   [:div {:class (if (> net-victories-main 0) "winner" "loser")} net-victories-main]
   [:div.gray points] [:div]])

(defn scoring-table [players]
  (into
    [:div.grid {:style {:grid-template-columns "80px 80px 60px 60px 60px 60px 60px 1fr"}}
     [:div.bold "Player"] [:div.bold "Opponent"] [:div.bold "Set Wins"]
     [:div.bold "Set Losses"] [:div.bold "Net-Win"] [:div.bold "Win-Main"] [:div.bold "Points"] [:div]]
    (->> players
         (mapcat (fn [{:keys [name matches victories losses points]}]
                   (concat
                     [[:div.row-line]]
                     (->> matches (mapcat #(match-line name %)))
                     [[:div name] [:div "TOTAL"] [:div.winner victories]
                      [:div.loser losses]
                      [:div {:class (if (> victories losses) "winner" "loser")} (- victories losses)]
                      [:div]
                      [:div.bold points] [:div]]))))))

(defn view []
  (let [year @(rf/subscribe [:selected-year])
        date @(rf/subscribe [:selected-session])
        {:keys [sets players]} @(rf/subscribe [:session-data year date])]
    [:<>
     [:div [:span.large.bold "Session " date]
      [:button.navigation {:on-click #(rf/dispatch [:show-year year])} "← " year]
      [:button.navigation {:on-click #(rf/dispatch [::set-addition])} "Add set..."]]
     [sets-table sets]
     [:div "Scoring"]
     [scoring-table players]]))

;; events

(rf/reg-event-db ::set-addition
  (fn [db _]
    (assoc-in db [:navigation :page] :add-set)))

