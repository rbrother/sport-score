(ns sports.year
  (:require [clojure.edn :as edn]
            [clojure.string :as s]
            [medley.core :refer [find-first]]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [sports.aws :as aws]
            [sports.calculations :as calc]
            [sports.chart :as chart]
            [sports.config :refer [debug?]]
            [sports.import :as import]
            [sports.routes :as routes]
            [sports.util :as util :refer [attr=]]
            [sports.widgets :as widgets]))

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

(defn session-rows [year year-data]
  (into [:<>]
        (->> year-data
             (sort-by first)
             (map-indexed
               (fn [index [date {:keys [players sets include-in-year-points?] :as _session}]]
                 [:<>
                  [:div (inc index)]
                  [:div.link {:style {:white-space "nowrap"}
                              :on-click #(routes/navigate! (routes/session-url year date))} date]
                  [:div (count sets)]
                  [player-points-cells players include-in-year-points?]]))
             reverse)))

(defn raw-data-editor []
  (let [year-data @(rf/subscribe [:current-year-data])
        state (reagent/atom {:text (util/pprint-str year-data)})]
    (fn []
      [:div.card
       [:div.card-title "Raw data editor"]
       [:div
        [:textarea {:rows 30, :cols 80, :value (:text @state)
                    :on-change (util/set-local-state state [:text])}]]
       [:div.button-row
        [:button.navigation
         {:on-click #(rf/dispatch [::raw-data-edited (:text @state)])}
         "Apply Raw Data Edits"]
        [:button.idle
         {:on-click #(rf/dispatch [::import-csv (:text @state)])}
         "Import CSV"]]])))

(defn totals-line [year-data]
  [:<>
   [:div] [:div.bold "TOTAL"] [:div]
   [player-points-cells (:players year-data) true]])

(defn points-table []
  (let [year @(rf/subscribe [:selected-year])
        year-data @(rf/subscribe [:year-summary year])]
    [:div.card
     [:div.card-title "Sessions"]
     [:div.table-scroll
      [:div.grid {:style {:grid-template-columns "20px 84px 30px repeat(3, 46px)"}}
       [:div.col-header "#"] [:div.col-header "Date"] [:div.col-header "Sets"]
       (into [:<>] (for [p calc/players] [:div.col-header p]))
       [:div.row-line]
       [totals-line year-data]
       [:div.row-line]
       [session-rows year (:sessions year-data)]
       [:div.row-line]
       [totals-line year-data]]]]))

(defn view []
  (let [year @(rf/subscribe [:selected-year])
        cumulative-data @(rf/subscribe [:cumulative-scores year])]
    [:div
     [widgets/header
      {:title (str "Year " year) :back-url (routes/years-url)
       :action [:button.navigation
                {:on-click #(routes/navigate! (routes/add-session-url year))}
                "+ Session"]}]
     [:div.page
      [points-table]
      [:div.chart-card [chart/score-development-chart cumulative-data]]
      (when debug? [raw-data-editor])]]))

;; EVENTS

(rf/reg-event-fx ::raw-data-edited
  (fn [{{{year :year} :navigation :as db} :db} [_ s]]
    (let [data (edn/read-string s)]
      {:db (assoc-in db [:years year] data)
       :dispatch [::aws/save]})))

(rf/reg-event-fx ::import-csv
  (fn [{{{year :year} :navigation :as db} :db} [_ s]]
    (let [data (import/year-data-from-csv s)]
      {:db (assoc-in db [:years year] data)
       :dispatch [::aws/save]})))
