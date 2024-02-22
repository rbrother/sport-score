(ns sports.year
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [cljs.pprint :refer [pprint]]
            [sports.log :as log]
            [sports.util :as util]))

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

(defn view []
  (let [year-data @(rf/subscribe [:year-data])
        players @(rf/subscribe [:players])]
    [:div
     ;; Add: number of sets played
     [:div.grid {:style {:grid-template-columns "100px 100px 100px 100px"}}
      [:div "Date"]
      (into [:<>]
            (for [p players] [:div p]))]
     [new-session-widget]
     [:textarea {:rows 10, :cols 80, :defaultValue (util/pprint-str year-data)
                 :on-blur #(rf/dispatch [::year-text-data-changed (-> % .-target .-value)])}]
     ]))

(rf/reg-event-db ::new-session
  (fn [{{year :year} :navigation :as db} [_ date-str]]
    (let [date (keyword date-str)]
      (-> db
          (assoc-in [:years year date] [])
          (assoc :navigation {:page :session, :session date})))))

(rf/reg-event-db ::year-text-data-changed
  (fn [db [_ s]]
    (print s)
    db
    ))