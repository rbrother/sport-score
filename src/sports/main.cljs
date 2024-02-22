(ns sports.main
  (:require-macros [reagent-mui.util :refer [react-component]])
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            ["@mui/material/styles" :refer [ThemeProvider createTheme]]
            [sports.years :as years]
            [sports.year :as year]
            [sports.session :as session]
            [sports.aws :as aws]))

(def theme-provider (r/adapt-react-class ThemeProvider))

;; VIEWS

(def dark-theme
  (createTheme (clj->js {:palette {:mode "dark"}})))

(defn main-panel []
  (let [page @(rf/subscribe [:page])
        status @(rf/subscribe [:status])]
    [theme-provider {:theme dark-theme}
     [:div
      [:div [:span.large.bold "Sport Tracker"]
       [:button.navigation {:on-click #(rf/dispatch [::aws/save])} "Save Data"]
       status]
      (case page
        :years [years/view]
        :year [year/view]
        :session [session/view])]]))

;; EVENTS

(rf/reg-event-fx ::initialize-db
  (fn [_ _]
    {:db {:years {:2023 {}, :2024 {}}
          :navigation {:page :years}}
     :dispatch [::aws/load-data]}))

