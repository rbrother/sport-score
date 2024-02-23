(ns sports.year
  (:require [clojure.string :as s]
            [clojure.pprint :refer [pprint]]
            [clojure.edn :as edn]
            [re-frame.core :as rf]
            [medley.core :refer [find-first]]
            [reagent.core :as reagent]
            [sports.calculations :as calc]
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
  (into [:<>]
        (for [[date {:keys [players sets]}] (sort-by first year-data)]
          [:<>
           [:div.link {:on-click #(rf/dispatch [::goto-date date])} date]
           [:div (count sets)]
           (into [:<>] (for [name calc/players]
                         (let [player (->> players (find-first (attr= :name name)))]
                           [:div (:points player)])))])))

(defn raw-data-editor []
  (let [year-data @(rf/subscribe [:current-year-data])
        state (reagent/atom {:text (util/pprint-str year-data)})]
    (fn []
      [:<>
       [:hr]
       [:div "Raw data editor"]
       [:div [:button.navigation
              {:on-click #(rf/dispatch [::raw-data-edited (:text @state)])}
              "Apply Raw Data Edits"]]
       [:div
        [:textarea {:rows 30, :cols 80, :value (:text @state)
                    :on-change (util/set-local-state state [:text])}]]])))

(defn points-table []
  (let [year @(rf/subscribe [:selected-year])
        year-data @(rf/subscribe [:year-summary year])
        player-cols (s/join " " (repeat (count calc/players) "100px"))]
    [:div.grid {:style {:grid-template-columns (str "100px 100px " player-cols)}}
     [:div.bold "Date"] [:div.bold "Sets"]
     (into [:<>] (for [p calc/players] [:div.bold p]))
     [:div.row-line]
     [session-rows (:sessions year-data)]
     [:div.row-line]
     (into [:<> [:div "TOTAL"] [:div]]
           (for [p calc/players]
             [:div.bold (->> (:players year-data)
                             (find-first (attr= :name p))
                             :points)]))]))

(defn view []
  [:div
   [:div [:span.large.bold "Year " @(rf/subscribe [:selected-year])]
    [:button.navigation {:on-click #(rf/dispatch [::all-years])} "← Years list"]]
   [:div "Points"]
   [points-table]
   [new-session-widget]
   (when debug? [raw-data-editor])])

;; EVENTS

(rf/reg-event-db ::new-session
  (fn [{{year :year} :navigation :as db} [_ date-str]]
    (let [date (keyword date-str)]
      (-> db
          (assoc-in [:years year date] [])
          (assoc :navigation {:page :session, :year year, :session date})))))

(rf/reg-event-db ::raw-data-edited
  (fn [{{year :year} :navigation :as db} [_ s]]
    (let [data (edn/read-string s)]
      (assoc-in db [:years year] data))))

(rf/reg-event-db ::goto-date
  (fn [db [_ date]]
    (update db :navigation
            #(assoc % :session date
                      :page :session))))

(rf/reg-event-db ::all-years
  (fn [db _]
    (assoc db :navigation {:page :years})))