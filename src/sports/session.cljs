(ns sports.session
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [cljs.pprint :refer [pprint]]
            [sports.calculations :as calc]
            [sports.log :as log]
            [sports.util :as util]))

(defn update-names [state index value]
  (let [other-index (if (= index 0) 1 0)
        other-player (get state other-index)
        change-other? (= (:name other-player) value)
        new-other (-> calc/players set (disj value) sort first)]
    (cond-> state
            true (assoc-in [index :name] value)
            change-other? (assoc-in [other-index :name] new-other))))

(defn player-score [index state]
  (let [player-data (get @state index)]
    [:<>
     [:select
      {:value (:name player-data)
       :on-change (fn [event]
                    (let [value (-> event .-target .-value)]
                      (swap! state update-names index value)))}
      (for [p calc/players]
        ^{:key p} [:option {:value p} p])]
     "Score"
     [:input.score {:type :text
                    :value (str (:score player-data))
                    :on-change (util/set-local-state state [index :score]
                                                     util/try-parse-int)}]]))

(defn new-game-widget []
  (let [local-state (reagent/atom [{:name (first calc/players) :score nil}
                                   {:name (second calc/players) :score nil}])]
    (fn []
      [:div
       [:div.grid {:style {:grid-template-columns "auto auto 1fr"
                           :align-items "center"}}
        [:div.winner "WINNER"] [:div [player-score 0 local-state]]
        [:div {:style {:grid-column "3" :grid-row "1/span 2"}}
         [:button.navigation
          {:on-click #(rf/dispatch [::add-game @local-state])}
          "Add Game"]]
        [:div.loser "LOSER"] [:div [player-score 1 local-state]]]])))

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
              [:div]])))))

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
  (pprint players)
  (into
    [:div.grid {:style {:grid-template-columns "80px 80px 60px 60px 60px 60px 60px 1fr"}}
     [:div.bold "Player"] [:div.bold "Opponent"] [:div.bold "Wins"]
     [:div.bold "Losses"] [:div.bold "Net-Win"] [:div.bold "Win-Main"] [:div.bold "Points"] [:div]]
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
      [:button.navigation {:on-click #(rf/dispatch [:show-year year])} "← " year]]
     [new-game-widget]
     [sets-table sets]
     [:div "Scoring"]
     [scoring-table players]]))

(rf/reg-event-db ::add-game [log/intercept]
  (fn [{{:keys [year session]} :navigation :as db} [_ values]]
    (update-in db [:years year session] #(conj % values))))

