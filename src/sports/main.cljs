(ns sports.main
  (:require [re-frame.core :as rf]
            [sports.aws :as aws]
            [sports.session :as session]
            [sports.year :as year]
            [sports.years :as years]))

;; VIEWS


(defn main-panel []
  (let [page @(rf/subscribe [:page])
        status @(rf/subscribe [:status])]
    [:div
     [:div [:span.large.bold "Sport Tracker"]
      [:button.navigation {:on-click #(rf/dispatch [::aws/save])} "Save Data"]
      status]
     (case page
       :years [years/view]
       :year [year/view]
       :session [session/view])]))

;; EVENTS

(rf/reg-event-fx ::initialize-db
  (fn [_ _]
    {:db {:years {:2023 {}, :2024 {}}
          :navigation {:page :years}}
     :dispatch [::aws/load-data]}))

