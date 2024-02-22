(ns sports.session
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [sports.log :as log]
            [sports.util :as util]))

(defn player-score [index state]
  (let [all-players @(rf/subscribe [:players])
        player-data (get @state index)]
    [:<>
     [:select
      {:value (:name player-data)
       :on-change (util/set-local-state state [index :name])}
      (for [p all-players]
        ^{:key p} [:option {:value p} p])]
     "Score"
     [:input.score {:type :text
                    :value (str (:score player-data))
                    :on-change (util/set-local-state state [index :score]
                                                     util/try-parse-int)}]]))

(defn new-game-widget []
  (let [all-players @(rf/subscribe [:players])
        local-state (reagent/atom [{:name (first all-players) :score ""}
                                   {:name (second all-players) :score ""}])]
    (fn []
      [:div
       [player-score 0 local-state] " vs "
       [player-score 1 local-state]
       [:button.navigation
        {:on-click #(rf/dispatch [::add-game @local-state])}
        "Add Game"]])))

(defn sets-table [sets]
  (into
    [:div.grid {:style {:width "500px" :grid-template-columns "auto auto auto auto 1fr"}}
     [:div.bold "#"] [:div.bold "Player 1"] [:div.bold "Score"] [:div.bold "Player 2"] [:div]]
    (->> sets
         (map-indexed
           (fn [index [{a-name :name, a-score :score, a-winner? :winner?}
                       {b-name :name, b-score :score, b-winner? :winner?}]]
             (let [a-style (if a-winner? "winner" "loser")
                   b-style (if b-winner? "winner" "loser")]
               [:<> [:div (inc index)]
                [:div {:class a-style} a-name]
                [:div
                 [:span {:class a-style} a-score] " - "
                 [:span {:class b-style} b-score]]
                [:div {:class b-style} b-name]
                [:div]]))))))

(defn match-line [name {:keys [opponent victories losses net-victories points]}]
  [[:div name] [:div opponent] [:div.winner victories]
   [:div.loser losses]
   [:div {:class (if (> net-victories 0) "winner" "loser")} net-victories]
   [:div.gray points] [:div]])

(defn scoring-table [players]
  (into
    [:div.grid {:style {:width "500px" :grid-template-columns "auto auto auto auto auto auto 1fr"}}
     [:div.bold "Player"] [:div.bold "Opponent"] [:div.bold "Wins"]
     [:div.bold "Losses"] [:div.bold "Net-Win"] [:div.bold "Points"] [:div]]
    (->> players
         (mapcat (fn [{:keys [name matches victories losses points]}]
                   (concat
                     [[:div.row-line]]
                     (->> matches (mapcat #(match-line name %)))
                     [[:div name] [:div "TOTAL"] [:div.winner victories]
                      [:div.loser losses]
                      [:div {:class (if (> victories losses) "winner" "loser")} (- victories losses)]
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

