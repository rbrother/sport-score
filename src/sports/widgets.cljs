(ns sports.widgets
  (:require [re-frame.core :as rf]
            [medley.core :refer [dissoc-in]]
            [sports.log :as log]))

;; Input-widget that stores the edited value in temporary data path until ENTER
;; is pressed and then copies to permanent data-path

(defn input [data-path opts]
  (let [base-value @(rf/subscribe [::get-value data-path])
        temp-value @(rf/subscribe [::get-temp-value data-path])
        coerce (or (:coerce opts) identity)
        changed-dispatch #(rf/dispatch [::set-value data-path (coerce (or temp-value base-value ""))])]
    [:input {:type      "text" :style (:style opts)
             :value     (or temp-value base-value "")
             :on-change #(rf/dispatch [::set-temp-value data-path (-> % .-target .-value)])
             :on-key-up #(when (= (.-key %) "Enter") (changed-dispatch))
             :on-blur   changed-dispatch
             :read-only (:read-only opts)}]))

(rf/reg-sub ::get-value
            (fn [db [_ path]] (get-in db path)))

(rf/reg-sub ::get-temp-value
            (fn [db [_ path]] (get-in db (into [:temp-input] path))))

(rf/reg-event-db ::set-value [log/intercept]
                 (fn [db [_ path value]]
                   (-> db (assoc-in path value)
                       (dissoc-in db (into [:temp-input] path)))))

(rf/reg-event-db ::set-temp-value [log/intercept]
                 (fn [db [_ path value]]
                   (assoc-in db (into [:temp-input] path) value)))