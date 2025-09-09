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
   :plugins {:title {:display true
                    :text "Cumulative Score Development"
                    :font {:size 16}}
            :legend {:display true
                    :position "top"}}
   :scales {:x {:display true
               :title {:display true
                      :text "Date"}
               :grid {:display true}}
           :y {:display true
               :title {:display true
                      :text "Cumulative Points"}
               :beginAtZero true
               :grid {:display true}}}
   :interaction {:intersect false
                :mode "index"}})

(defn score-development-chart [cumulative-data]
  "React component for displaying the score development chart"
  (if (and cumulative-data (seq cumulative-data)
           (some #(seq (:data %)) cumulative-data))
    (let [chart-data (prepare-chart-data cumulative-data)]
      [:div {:style {:width "100%" :height "500px" :margin-top "30px" :margin-bottom "20px"}}
       [:> Line {:data chart-data
                 :options chart-options}]])
    [:div {:style {:margin-top "30px" :margin-bottom "20px" :padding "20px" :background-color "#f5f5f5" :border-radius "8px"}}
     [:h3 {:style {:margin-top "0"}} "Score Development Over Time"]
     [:p "No data available for chart. Add some sessions with all players present to see the chart."]]))
