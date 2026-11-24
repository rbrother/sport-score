(ns sports.payments
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [reagent.core :as reagent]
            [sports.aws :as aws]
            [sports.calculations :as calc]
            [sports.routes :as routes]))

(defn tracked-date? [date]
  (let [s (when date (name date))]
    (and s (re-matches #"\d{4}-\d{2}-\d{2}" s)
         (not (neg? (compare s "2026-01-01"))))))

(defn missing-payer-sessions [years]
  (let [sessions (for [[year dates] years
                       [date session] dates
                       :when (tracked-date? date)]
                   {:year year :date date :payer (get-in session [:payment :payer])})
        first-paid-date (first (sort (keep #(when (:payer %) (:date %)) sessions)))]
    (if first-paid-date
      (into #{} (comp (filter #(and (not (:payer %))
                                     (pos? (compare (:date %) first-paid-date))))
                      (map (juxt :year :date)))
            sessions)
      #{})))

(defn participants [session]
  (let [names (set (map :name (mapcat identity (calc/session-sets session))))]
    (when (and (<= 2 (count names) 3)
               (every? (set calc/players) names))
      (filterv names calc/players))))

(defn parse-cost [s]
  (when-let [[_ euros cents] (re-matches #"(\d{1,7})(?:[.,](\d{1,2}))?" (str/trim s))]
    (let [amount (+ (* 100 (js/parseInt euros 10))
                    (js/parseInt (subs (str (or cents "") "00") 0 2) 10))]
      (when (<= amount 100000000) amount))))

(defn payment-error [date session cost payer]
  (let [amount (parse-cost cost)]
    (cond
      (not session) "This session no longer exists."
      (not (tracked-date? date)) "Payments can only be recorded from 2026 onwards."
      (str/blank? cost) nil
      (nil? amount) "Enter a cost from 0 to 1,000,000 euros, with at most two decimal places."
      (not (some #{payer} calc/players)) "Select who paid."
      (and (pos? amount) (not (participants session)))
      "Record sets for two or three known players before saving a cost."
      :else nil)))

(defn shares [date session cost-cents]
  (let [players (participants session)
        n (count players)
        base (quot cost-cents n)
        remainder (mod cost-cents n)
        hash (reduce (fn [h c] (mod (+ (* h 31) (.charCodeAt c 0)) 2147483647))
                     0 (seq (name date)))
        offset (mod hash n)]
    (into {} (map-indexed (fn [i player]
                            [player (+ base (if (< (mod (- i offset) n) remainder) 1 0))])
                          players))))

(defn balances [years balance-year]
  (reduce
    (fn [result [year sessions]]
      (reduce
        (fn [result [date session]]
          (let [{:keys [cost-cents payer] :as payment} (when (map? session) (:payment session))]
            (if (and (tracked-date? date)
                     (str/starts-with? (name date) (str (name balance-year) "-"))
                     payment (pos? cost-cents))
              (if (participants session)
                (let [result (update-in result [:balances payer] + cost-cents)]
                  (reduce-kv (fn [acc player share] (update-in acc [:balances player] - share))
                             result (shares date session cost-cents)))
                (update result :pending conj {:year year :date date}))
              result)))
        result sessions))
    {:balances (zipmap calc/players (repeat 0)) :pending []}
    years))

(defn format-euros [cents]
  (let [euros (js/Math.round (/ (js/Math.abs cents) 100))]
    (str (when (and (neg? cents) (pos? euros)) "-") "€" euros)))

(defn suggested-payer [balances session]
  (let [{:keys [cost-cents payer] :as payment} (:payment session)
        balances (if (and payment (pos? cost-cents) (participants session))
                   (update balances payer - cost-cents)
                   balances)]
    (reduce (fn [best player]
              (if (< (get balances player) (get balances best)) player best))
            calc/players)))

(defn balance-card [year]
  (let [{:keys [balances pending]} @(rf/subscribe [:payment-balances year])]
    [:div.card
     [:div.card-title (str "Rent balance " (name year))]
     [:div.gray "Resets each year. Positive is a deposit; negative is debt."]
     [:div.gray "Displayed amounts are rounded to euros; calculations use cents."]
     [:div.grid {:style {:grid-template-columns "max-content max-content"}}
      (for [player calc/players]
        ^{:key player} [:<>
                        [:div player]
                        [:div {:class (cond (pos? (get balances player)) "winner"
                                            (neg? (get balances player)) "loser")}
                         (format-euros (get balances player))]])]
     (when (seq pending)
       [:div.loser {:role "alert"}
        "These paid sessions are excluded until their participants are corrected:"
        (for [{:keys [year date]} (sort-by :date pending)]
          ^{:key (str year "/" date)}
          [:div.link {:on-click #(routes/navigate! (routes/session-url year date))}
           (name date)])])]))

(defn session-payment [year date]
  (let [session @(rf/subscribe [:session-record year date])
        payment (:payment session)
        state (reagent/atom {:cost (if payment (.toFixed (/ (:cost-cents payment) 100) 2) "")
                             :payer (or (:payer payment) "")
                             :error nil})]
    (fn [year date]
      (let [session @(rf/subscribe [:session-record year date])
            payment (:payment session)
            session-players (participants session)
            balance-year (keyword (subs (name date) 0 4))
            balances (:balances @(rf/subscribe [:payment-balances balance-year]))
            suggestion (suggested-payer balances session)]
        [:div.card
         [:div.card-title "Hall rent"]
         (if payment
           [:div (str "Cost: " (format-euros (:cost-cents payment)) " · Paid by " (:payer payment))]
           [:div.gray "No cost recorded (counts as €0)."])
         [:div.gray (if session-players
                      (str "Sharing: " (str/join ", " session-players))
                      "Record sets for two or three known players to calculate shares.")]
         (when (and payment (pos? (:cost-cents payment)) (not session-players))
           [:div.loser {:role "alert"} "This payment is excluded from balances until participants are known."])
         [:label.field-label {:for "rent-cost"} "Total cost (€)"]
         [:div.gray "Displayed cost is rounded; enter the exact amount including cents."]
         [:input {:id "rent-cost" :type "text" :input-mode "decimal" :style {:width "100%"}
                  :placeholder "Leave blank for no cost"
                  :value (:cost @state)
                  :on-change #(swap! state assoc :cost (.. % -target -value) :error nil)}]
         [:label.field-label {:for "rent-payer"} (str "Paid by (Suggestion: " suggestion ")")]
         [:select {:id "rent-payer" :style {:width "100%"} :value (:payer @state)
                   :on-change #(swap! state assoc :payer (.. % -target -value) :error nil)}
          [:option {:value ""} "Select payer"]
          (for [player calc/players] ^{:key player} [:option {:value player} player])]
         (when-let [error (:error @state)]
           [:div.loser {:role "alert"} error])
         [:button.navigation
          {:on-click (fn []
                       (let [{:keys [cost payer]} @state
                             error (payment-error date session cost payer)]
                         (if error
                           (swap! state assoc :error error)
                           (rf/dispatch [::save-payment year date cost payer]))))}
          "Save rent"]]))))

(rf/reg-event-fx ::save-payment
  (fn [{:keys [db]} [_ year date cost payer]]
    (let [session (get-in db [:years year date])
          error (payment-error date session cost payer)]
      (if error
        {:db (assoc db :status error)}
        (let [updated (if (str/blank? cost)
                        (calc/session-sets session)
                        {:sets (calc/session-sets session)
                         :payment {:cost-cents (parse-cost cost) :payer payer}})]
          {:db (assoc-in db [:years year date] updated)
           :dispatch [::aws/save]})))))
