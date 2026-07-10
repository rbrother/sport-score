(ns sports.chart
  (:require [reagent.core :as reagent]
            ["chart.js/auto" :as Chart]
            ["react-chartjs-2" :refer [Line]]))

(def player-colors
  {"Roope" "#FF6384"
   "Kari" "#36A2EB" 
   "Niklas" "#FFCE56"})

(defn prepare-chart-data [cumulative-data]
  "Transform cumulative score data into Chart.js format"
  (let [all-dates (->> cumulative-data
                      (mapcat #(:data %))
                      (map :date)
                      distinct
                      sort)
        datasets (->> cumulative-data
                     (map (fn [{:keys [name data]}]
                            (let [data-map (->> data
                                               (map (fn [entry] [(:date entry) (:cumulative entry)]))
                                               (into {}))]
                              {:label name
                               :data (->> all-dates
                                         (map (fn [date]
                                                (get data-map date 0)))
                                         vec)
                               :borderColor (get player-colors name "#999999")
                               :backgroundColor (str (get player-colors name "#999999") "20")
                               :fill false
                               :tension 0.1
                               :pointRadius 4
                               :pointHoverRadius 6})))
                     vec)]
    {:labels all-dates
     :datasets datasets}))

(def chart-options
  {:responsive true
   :maintainAspectRatio false
   :color "#9aa2b1"
   :plugins {:title {:display true
                    :text "Cumulative Score Development"
                    :color "#eef0f4"
                    :font {:size 15 :weight "600"}}
            :legend {:display true
                    :position "top"
                    :labels {:color "#eef0f4" :usePointStyle true}}}
   :scales {:x {:display true
               :title {:display true
                      :text "Date"
                      :color "#9aa2b1"}
               :ticks {:color "#9aa2b1"}
               :grid {:display true :color "rgba(255,255,255,0.06)"}}
           :y {:display true
               :title {:display true
                      :text "Cumulative Points"
                      :color "#9aa2b1"}
               :ticks {:color "#9aa2b1"}
               :beginAtZero true
               :grid {:display true :color "rgba(255,255,255,0.06)"}}}
   :interaction {:intersect false
                :mode "index"}})

(defn score-development-chart [cumulative-data]
  "React component for displaying the score development chart"
  (if (and cumulative-data (seq cumulative-data)
           (some #(seq (:data %)) cumulative-data))
    (let [chart-data (prepare-chart-data cumulative-data)]
      [:div {:style {:width "100%" :height "360px" :padding "6px"}}
       [:> Line {:data chart-data
                 :options chart-options}]])
    [:div.empty-state
     [:div.bold "Score Development Over Time"]
     [:p "No data available for chart. Add some sessions with all players present to see the chart."]]))
