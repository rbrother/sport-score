(ns sports.session
  (:require [cljs.pprint :refer [pprint]]
            [clojure.edn :as edn]
            [medley.core :refer [find-first]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [sports.aws :as aws]
            [sports.calculations :as calc]
            [sports.routes :as routes]
            [sports.util :as util]
            [sports.util :refer [attr=]]
            [sports.widgets :as widgets]))

(defn sets-table [sets]
  [:div.card
   [:div.card-title "Sets"]
   [:div.table-scroll
    (into
      [:div.grid {:style {:grid-template-columns "max-content max-content max-content max-content"}}
       [:div.col-header "#"] [:div.col-header "Winner"] [:div.col-header "Score"] [:div.col-header "Loser"]]
      (->> sets
           (map-indexed
             (fn [index [{winner-name :name, winner-score :score}
                         {loser-name :name, loser-score :score}]]
               [:<> [:div (inc index)]
                [:div.winner {:style {:white-space "nowrap"}} winner-name]
                [:div {:style {:white-space "nowrap"}}
                 [:span.winner winner-score] " - "
                 [:span.loser loser-score]]
                [:div.loser {:style {:white-space "nowrap"}} loser-name]]))
           reverse))]])

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
                          1 ["text-win" "text-lose"]
                          0 [nil nil]
                          -1 ["text-lose" "text-win"])]
    [^{:key (str p1 "-" p2 "-label")}
     [:div {:style {:white-space "nowrap"}} [:span {:class class1} p1] " - " [:span {:class class2} p2]]
     ^{:key (str p1 "-" p2 "-victories")}
     [:div {:style {:white-space "nowrap"}}
      [:span {:class class1}
       victories1 (when (not= victories1 vic-main1) [:span "(" vic-main1 ")"])] " - "
      [:span {:class class2}
       victories2 (when (not= victories2 vic-main2) [:span "(" vic-main2 ")"])]]
     ^{:key (str p1 "-" p2 "-points")}
     [:div {:style {:white-space "nowrap"}}
      [:span {:class class1} points1] " - " [:span {:class class2} points2]]]))

(defn totals-row [players-data p high low]
  (let [points (->> players-data (find-first (attr= :name p)) :points)
        style (cond (= points high) "winner" (= points low) "loser" :else nil)]
    [^{:key (str "total-name-" p)} [:div.bold p]
     ^{:key (str "total-points-" p)} [:div {:class style} points]]))

(defn totals-table [players-data]
  (let [all-points (->> players-data (map :points))
        high (apply max all-points)
        low (apply min all-points)
        sorted-players (->> calc/players
                             (sort-by (fn [p] (->> players-data (find-first (attr= :name p)) :points)))
                             reverse)]
    [:div.card
     [:div.card-title "Totals"]
     [:div.grid {:style {:grid-template-columns "max-content max-content"}}
      (->> sorted-players (mapcat #(totals-row players-data % high low)))]]))

(defn scoring-table [players-data]
  [:div.card
   [:div.card-title "Head-to-head"]
   [:div.grid {:style {:grid-template-columns "max-content max-content max-content"}}
    [:div.col-header "Pair"] [:div.col-header "Sets"] [:div.col-header "Points"]
    [:div.row-line]
    (->> calc/player-pairs
         (mapcat (fn [[p1 p2]] (scoring-row players-data p1 p2))))]])

(defn view []
  (let [year @(rf/subscribe [:selected-year])
        date @(rf/subscribe [:selected-session])
        {:keys [sets players]} @(rf/subscribe [:session-data year date])]
    [:div
     [widgets/header
      {:title (str "Session " date) :back-url (routes/year-url year)
       :action [:button.navigation
                {:on-click #(routes/navigate! (routes/add-set-url year date))}
                "+ Set"]}]
     [:div.page
      [totals-table players]
      [scoring-table players]
      [sets-table sets]
      [:div.center
       [:button.idle
        {:on-click #(routes/navigate! (routes/raw-data-url year date))}
        "Edit Session Raw Data"]]]]))

(defn raw-data-editor-view []
  (let [year @(rf/subscribe [:selected-year])
        date @(rf/subscribe [:selected-session])
        {:keys [sets]} @(rf/subscribe [:session-data year date])
        state (reagent/atom {:text (util/pprint-str sets)})]
    (fn []
      [:div
       [widgets/header {:title (str "Edit Raw Data " date)
                         :back-url (routes/session-url year date)}]
       [:div.page
        [:div.card
         [:textarea {:rows 20, :cols 100, :value (:text @state)
                     :on-change (util/set-local-state state [:text])}]]
        [:button.navigation.primary-block
         {:on-click #(rf/dispatch [::raw-data-edited (:text @state)])}
         "Save"]]])))

;; events

(rf/reg-event-fx ::raw-data-edited
  (fn [{{{year :year session :session} :navigation :as db} :db} [_ s]]
    (let [data (edn/read-string s)]
      {:db (assoc-in db [:years year session] data)
       :navigate! (routes/session-url year session)
       :dispatch [::aws/save]})))

