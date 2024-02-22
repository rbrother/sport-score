(ns sports.session
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [cljs.pprint :refer [pprint]]
            [sports.log :as log]))

(defn new-game-widget []
  (let [local-state (reagent/atom {})]
    [:<>
     [:div "Player A" [:input {:type :select}] "Score" [:input.score {:type :text}]]
     [:div "Player B" [:input {:type :select}] "Score" [:input.score {:type :text}]]
     [:button.navigation "Add Game"]]
    ))

(defn view []
  (let [date @(rf/subscribe [:selected-session])]
    [:<>
     [:h2 "Session " date]
     [:div.grid {:style {:grid-template-columns "50px 100px 50px 100px"}}
      [:div "#"] [:div "Player 1"] [:div "Score"] [:div "Player 2"]]
     ;; rows of games
     [new-game-widget]]
    ))