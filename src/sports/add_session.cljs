(ns sports.add-session
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [sports.aws :as aws]
            [sports.log :as log]
            [sports.routes :as routes]
            [sports.widgets :as widgets]))

(defn pad2 [n] (let [s (str n)] (if (= 1 (count s)) (str "0" s) s)))

(defn today-str []
  (let [d (js/Date.)]
    (str (.getFullYear d) "-" (pad2 (inc (.getMonth d))) "-" (pad2 (.getDate d)))))

(defn view []
  (let [local-state (reagent/atom {:date (today-str)})
        {:keys [year]} @(rf/subscribe [:navigation])]
    (fn []
      [:div
       [widgets/header {:title "Add Session" :back-url (routes/year-url year)}]
       [:div.page
        [:div.card
         [:label.field-label "Date (YYYY-MM-DD)"]
         [:input {:type :text
                  :style {:width "100%"}
                  :value (:date @local-state)
                  :on-change (fn [event]
                               (let [value (-> event .-target .-value)]
                                 (swap! local-state assoc :date value)))}]]
        [:button.navigation.primary-block
         {:on-click #(rf/dispatch [::new-session year (:date @local-state)])}
         "Create Session"]]])))

;; events

(rf/reg-event-fx ::new-session [log/intercept]
  (fn [{:keys [db]} [_ year date-str]]
    (let [date (keyword date-str)]
      (if-not (re-matches #"\d\d\d\d-\d\d-\d\d" date-str)
        {:db (assoc db :status "Invalid date format, must use YYYY-MM-DD")}
        {:db (assoc-in db [:years year date] [])
         :navigate! (routes/session-url year date)
         :dispatch [::aws/save]}))))
