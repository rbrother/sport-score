(ns sports.add-session
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [sports.log :as log]))

(defn pad2 [n] (let [s (str n)] (if (= 1 (count s)) (str "0" s) s)))

(defn today-str []
  (let [d (js/Date.)]
    (str (.getFullYear d) "-" (pad2 (inc (.getMonth d))) "-" (pad2 (.getDate d)))))

(defn view []
  (let [local-state (reagent/atom {:date (today-str)})
        {:keys [year]} @(rf/subscribe [:navigation])]
    (fn []
      [:div
       [:div [:span.large.bold "Add Session"]
        [:button.navigation {:on-click #(rf/dispatch [::cancel])} "← Back to Year"]]
       [:div
        "Date (YYYY-MM-DD) "
        [:input {:type :text
                 :value (:date @local-state)
                 :on-change (fn [event]
                              (let [value (-> event .-target .-value)]
                                (swap! local-state assoc :date value)))}]
        [:button.navigation
         {:on-click #(rf/dispatch [::new-session year (:date @local-state)])}
         "OK"]]])))

;; events

(rf/reg-event-db ::cancel [log/intercept]
  (fn [db _] (assoc-in db [:navigation :page] :year)))

(rf/reg-event-db ::new-session [log/intercept]
  (fn [db [_ year date-str]]
    (let [date (keyword date-str)]
      (if-not (re-matches #"\d\d\d\d-\d\d-\d\d" date-str)
        (assoc db :status "Invalid date format, must use YYYY-MM-DD")
        (-> db
            (assoc-in [:years year date] [])
            (assoc :navigation {:page :session, :year year, :session date}))))))
