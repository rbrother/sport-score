(ns sports.session
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [cljs.pprint :refer [pprint]]
            [sports.log :as log]
            [sports.util :as util]))

(defn player-score [index state]
  (let [all-players @(rf/subscribe [:players])
        player-data (get @state index)]
    [:div "Player " (inc index)
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
       [player-score 0 local-state]
       [player-score 1 local-state]
       [:button.navigation
        {:on-click #(rf/dispatch [::add-game @local-state])}
        "Add Game"]])))

(defn set-rows []
  (let [sets @(rf/subscribe [:session-data])]
    (into [:<>]
          (->> sets
               (map-indexed
                 (fn [index [a b]]
                   [:<> [:div (inc index)]
                    [:div (:name a)]
                    [:div (:score a) " - " (:score b)]
                    [:div (:name b)]
                    [:div]]))))))

(defn view []
  (let [date @(rf/subscribe [:selected-session])
        year @(rf/subscribe [:selected-year])]
    [:<>
     [:div [:span.large.bold "Session " date]
      [:button.navigation {:on-click #(rf/dispatch [:show-year year])} "← " year]]
     [:div.grid {:style {:width "500px" :grid-template-columns "auto auto auto auto 1fr"}}
      [:div.bold "#"] [:div.bold "Player 1"] [:div.bold "Score"] [:div.bold "Player 2"] [:div ]
      [set-rows]]
     [new-game-widget]]))

(rf/reg-event-db ::add-game [log/intercept]
  (fn [{{:keys [year session]} :navigation :as db} [_ values]]
    (update-in db [:years year session] #(conj % values))))

