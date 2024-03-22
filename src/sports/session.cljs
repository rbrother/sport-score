(ns sports.session
  (:require [medley.core :refer [find-first]]
            [re-frame.core :as rf]
            [sports.calculations :as calc]
            [sports.util :refer [attr=]]))

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

(defn scores-a-b [players-data p1 p2]
  (->> players-data
       (find-first (attr= :name p1)) :matches
       (find-first (attr= :opponent p2))))

(defn hide-zero [val] (when (> val 0) val))

(defn scoring-row [players-data p1 p2]
  (let [scores1 (scores-a-b players-data p1 p2)
        scores2 (scores-a-b players-data p2 p1)
        {points1 :points, victories1 :victories, vic-main1 :victories-main} scores1
        {points2 :points, victories2 :victories, vic-main2 :victories-main} scores2
        [class1 class2] (case (compare points1 points2)
                          1 ["winner" "loser"]
                          0 [nil nil]
                          -1 ["loser" "winner"])]
    [[:div [:span {:class class1} p1] " - " [:span {:class class2} p2]]
     [:div
      [:span {:class class1}
       victories1 (when (not= victories1 vic-main1) [:span "(" vic-main1 ")"])] " - "
      [:span {:class class2}
       victories2 (when (not= victories2 vic-main2) [:span "(" vic-main2 ")"])]]
     (for [p calc/players]
       ^{:key p} [:div (hide-zero (cond (= p p1) points1
                                        (= p p2) points2
                                        :else 0))])
     [:div]]))

(defn scoring-table [players-data]
  [:div.grid {:style {:grid-template-columns "120px 80px 60px 60px 60px 1fr"}}
   [:div.bold "Pair"] [:div.bold "Sets"]
   (for [p calc/players] ^{:key p} [:div.bold p])
   [:div]
   (->> calc/player-pairs
        (mapcat (fn [[p1 p2]] (scoring-row players-data p1 p2))))
   [:div.row-line]
   [:div.bold "TOTAL"] [:div]
   (for [p calc/players]
     ^{:key (str "total-" p)}
     [:div.bold (->> players-data (find-first (attr= :name p)) :points)])
   [:div]])

(defn view []
  (let [year @(rf/subscribe [:selected-year])
        date @(rf/subscribe [:selected-session])
        {:keys [sets players]} @(rf/subscribe [:session-data year date])]
    [:<>
     [:div [:span.large.bold "Session " date]
      [:button.navigation {:on-click #(rf/dispatch [:show-year year])} "← " year]
      [:button.navigation {:on-click #(rf/dispatch [::set-addition])} "Add set..."]]
     [scoring-table players]
     [sets-table sets]]))

;; events

(rf/reg-event-db ::set-addition
  (fn [db _]
    (assoc-in db [:navigation :page] :add-set)))

