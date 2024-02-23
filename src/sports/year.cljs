(ns sports.year
  (:require [clojure.string :as s]
            [re-frame.core :as rf]
            [medley.core :refer [find-first]]
            [reagent.core :as reagent]
            [sports.config :refer [debug?]]
            [sports.util :as util :refer [attr=]]))

(defn new-session-widget []
  (let [local-state (reagent/atom {})]
    (fn []
      [:div
       "Date (YYYY-MM-DD) "
       [:input {:type :text
                :on-change (fn [event]
                             (let [value (-> event .-target .-value)]
                               (swap! local-state #(assoc % :date value))))}]
       [:button.navigation
        {:on-click #(rf/dispatch [::new-session (:date @local-state)])}
        "Add Session"]])))

(defn session-rows [year-data]
  (let [players-all @(rf/subscribe [:players])]
    (into [:<>]
          (for [[date {:keys [players sets]}] (sort-by first year-data)]
            [:<>
             [:div.link {:on-click #(rf/dispatch [::goto-date date])} date]
             [:div (count sets)]
             (into [:<>] (for [name players-all]
                           (let [player (->> players (find-first (attr= :name name)))]
                             [:div (:points player)])))]))))

(defn view []
  (let [year @(rf/subscribe [:selected-year])
        year-data @(rf/subscribe [:year-summary year])
        players @(rf/subscribe [:players])
        player-cols (s/join " " (repeat (count players) "100px"))]
    [:div
     [:div [:span.large.bold "Year " year]
      [:button.navigation {:on-click #(rf/dispatch [::all-years])} "← Years list"]]
     [:div "Points"]
     [:div.grid {:style {:grid-template-columns (str "100px 100px " player-cols)}}
      [:div.bold "Date"] [:div.bold "Sets"]
      (into [:<>] (for [p players] [:div.bold p]))
      [session-rows (:sessions year-data)]
      (into [:<> [:div "TOTAL"] [:div "..."]] (for [p players] [:div.bold "..."]))
      ]
     [new-session-widget]
     (when debug?
       [:<>
        [:div "Debugging data"]
        [:textarea {:rows 10, :cols 80, :defaultValue (util/pprint-str year-data)
                    :on-blur #(rf/dispatch [::year-text-data-changed (-> % .-target .-value)])}]])]))

;; EVENTS

(rf/reg-event-db ::new-session
  (fn [{{year :year} :navigation :as db} [_ date-str]]
    (let [date (keyword date-str)]
      (-> db
          (assoc-in [:years year date] [])
          (assoc :navigation {:page :session, :year year, :session date})))))

(rf/reg-event-db ::year-text-data-changed
  (fn [db [_ s]]
    (print s)
    db))

(rf/reg-event-db ::goto-date
  (fn [db [_ date]]
    (print date)
    (update db :navigation
            #(assoc % :session date
                      :page :session))))

(rf/reg-event-db ::all-years
  (fn [db _]
    (assoc db :navigation {:page :years})))