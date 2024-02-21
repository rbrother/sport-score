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
  (let [page @(rf/subscribe [:page])]
    [theme-provider {:theme dark-theme}
     [:div
      [:h2 "Sport Tracker"]
      [:button.navigation {:on-click #(rf/dispatch [::aws/save])} "Save Data"]
      (case page
          :years [years/view]
          :year [year/view]
          :session [session/view])]]))

;; SUBS

(rf/reg-sub :navigation (fn [db _] (:navigation db)))

(rf/reg-sub :page :<- [:navigation] (fn [nav _] (:page nav)))

;; EVENTS

(rf/reg-event-fx ::initialize-db
  (fn [_ _]
    {:db {:years {}
          :navigation {:page :years}}
     :dispatch [::aws/load-data]}))

