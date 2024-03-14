(ns sports.year
  (:require [clojure.edn :as edn]
            [clojure.string :as s]
            [clojure.pprint :refer [pprint]]
            [medley.core :refer [find-first]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [sports.calculations :as calc]
            [sports.import :as import]
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

(defn mark-ignored [s ignore?]
  (if ignore? (str "(" s ")") s))

(defn player-points-cells [players-data include?]
  (into [:<>]
        (for [name calc/players]
          (let [max-points (->> players-data (map :points) (apply max))
                min-points (->> players-data (map :points) (apply min))
                {:keys [points]} (->> players-data (find-first (attr= :name name)))
                style (cond
                        (= points max-points) "winner"
                        (= points min-points) "loser"
                        :else "")]
            [:div {:class style}
              (some-> points (.toFixed 1)
                     (mark-ignored (not include?)))]))))

(defn session-rows [year-data]
  (into [:<>]
        (for [[date {:keys [players sets include-in-year-points?] :as _session}]
              (reverse (sort-by first year-data))]
          [:<>
           [:div.link {:on-click #(rf/dispatch [::goto-date date])} date]
           [:div (count sets)]
           [player-points-cells players include-in-year-points?]
           [:div] ;; 1fr padding
           ])))

(defn raw-data-editor []
  (let [year-data @(rf/subscribe [:current-year-data])
        state (reagent/atom {:text (util/pprint-str year-data)})]
    (fn []
      [:<>
       [:hr]
       [:div "Raw data editor"]
       [:div [:button.navigation
              {:on-click #(rf/dispatch [::raw-data-edited (:text @state)])}
              "Apply Raw Data Edits"]
        [:button.navigation
         {:on-click #(rf/dispatch [::import-csv (:text @state)])}
         "Import CSV"]]
       [:div
        [:textarea {:rows 30, :cols 80, :value (:text @state)
                    :on-change (util/set-local-state state [:text])}]]])))

(defn totals-line [year-data]
  [:<>
   [:div "TOTAL"] [:div]
   [player-points-cells (:players year-data) true]])

(defn points-table []
  (let [year @(rf/subscribe [:selected-year])
        year-data @(rf/subscribe [:year-summary year])
        player-cols (s/join " " (repeat (count calc/players) "80px"))]
    [:div.grid {:style {:grid-template-columns (str "100px auto " player-cols " 1fr")}}
     [:div.bold "Date"] [:div.bold "Sets"]
     (into [:<>] (for [p calc/players] [:div.bold p])) [:div]
     [:div.row-line]
     [totals-line year-data]
     [:div.row-line]
     [session-rows (:sessions year-data)]
     [:div.row-line]
     [totals-line year-data]]))

(defn view []
  [:div
   [:div [:span.large.bold "Year " @(rf/subscribe [:selected-year])]
    [:button.navigation {:on-click #(rf/dispatch [::all-years])} "← Years list"]]
   [new-session-widget]
   [points-table]
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

(rf/reg-event-db ::import-csv
  (fn [{{year :year} :navigation :as db} [_ s]]
    (let [data (import/year-data-from-csv s)]
      (assoc-in db [:years year] data))))

(rf/reg-event-db ::goto-date
  (fn [db [_ date]]
    (update db :navigation
            #(assoc % :session date
                      :page :session))))

(rf/reg-event-db ::all-years
  (fn [db _]
    (assoc db :navigation {:page :years})))