(ns sports.add-set
  (:require [re-frame.core :as rf]
            [reagent.core :as reagent]
            [sports.calculations :as calc]
            [sports.log :as log]
            [sports.util :as util]))

(defn update-names [state index value]
  (let [other-index (if (= index 0) 1 0)
        other-player (get state other-index)
        change-other? (= (:name other-player) value)
        new-other (-> calc/players set (disj value) sort first)]
    (cond-> state
            true (assoc-in [index :name] value)
            change-other? (assoc-in [other-index :name] new-other))))

(defn update-score [state index points]
  (cond-> state
          true (assoc-in [index :score] points)
          (= 0 index) (assoc-in [1 :score] (- points 2)) ;; set loser score
          ))

(defn player-selector [index state]
  (let [player-data (get @state index)]
    [:<>
     (for [p calc/players]
       ^{:key p} [:button
                  {:class (if (= p (:name player-data)) "navigation" "idle")
                   :style {:min-width "100px"}
                   :on-click (fn [] (swap! state update-names index p))}
                  p])]))

(defn point-range [index players-data]
  (let [winner-score (get-in players-data [0 :score])]
    (if (zero? index) ;; winner?
      (range 11 20) ;; winner points
      (cond
        (not winner-score) []
        (= winner-score 11) (range 0 10)
        :else [(- winner-score 2)] ;; with large score, difference must be 2
        ))))

(defn score-selector [index state]
  (let [player-data (get @state index)]
    (into [:div]
          (for [points (point-range index @state)]
            [:button
             {:class (if (= points (:score player-data)) "navigation" "idle")
              :style {:margin "4px"}
              :on-click (fn [] (swap! state update-score index points))}
             points]))))

(defn view []
  (let [local-state (reagent/atom [{:name nil :score nil}
                                   {:name nil :score nil}])
        {:keys [session]} @(rf/subscribe [:navigation])]
    (fn []
      (let [ok-to-add? (and (get-in @local-state [0 :name])
                            (get-in @local-state [1 :name]))]
        [:div
         [:div [:span.large.bold "New Set to Session " session]
          [:button.navigation {:on-click #(rf/dispatch [::cancel])} "← Back to Session"]]
         [:div.grid {:style {:grid-template-columns "min-content auto"
                             :align-items "center"}}
          [:div.winner "WINNER"] [:div [player-selector 0 local-state]]
          [:div "Score"] [score-selector 0 local-state]]
         [:div.grid {:style {:grid-template-columns "min-content auto"
                             :align-items "center"}}
          [:div.loser "LOSER"] [:div [player-selector 1 local-state]]
          [:div "Score"] [score-selector 1 local-state]]
         (when ok-to-add?
           [:div [:button.navigation
                  {:on-click (when ok-to-add? #(rf/dispatch [::add-game @local-state]))}
                  "ADD SET !"]])]))))

;; events

(rf/reg-event-db ::cancel [log/intercept]
  (fn [db _] (assoc-in db [:navigation :page] :session)))

(rf/reg-event-fx ::add-game [log/intercept]
  (fn [{{{:keys [year session]} :navigation :as db} :db} [_ values]]
    {:db (-> db (update-in [:years year session] #(conj % values))
             (assoc-in [:navigation :page] :session))
     :dispatch [:sports.aws/save]}))