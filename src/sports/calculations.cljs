(ns sports.calculations
  (:require [medley.core :refer [map-vals]]
            [clojure.pprint :refer [pprint]]
            [sports.util :refer [attr=]]))

(def players ["Roope", "Kari", "Niklas"])

(def player-pairs
  (->> (for [p1 players, p2 players, :when (not= p1 p2)] #{p1 p2})
       distinct
       (map vec)
       vec))

(defn count-victories [sets player opponent include-pre?]
  (cond->> sets
           (not include-pre?) (filter (fn [[_ loser]] (:score loser)))
           true (filter (fn [[{winner-name :name} {loser-name :name}]]
                          (and (= winner-name player)
                               (= loser-name opponent))))
           true count))

(defn collect-player-victories [sets player session-payers]
  (let [opponents (disj (set session-payers) player)]
    (->> opponents, sort
         (mapv (fn [opp]
                 (let [player-victories (count-victories sets player opp true)
                       player-losses (count-victories sets opp player true)
                       player-victories-main (count-victories sets player opp false)
                       player-losses-main (count-victories sets opp player false)
                       net-victories (- player-victories player-losses)
                       net-victories-main (- player-victories-main player-losses-main)]
                   {:opponent opp
                    :victories player-victories
                    :victories-main player-victories-main
                    :losses player-losses
                    :losses-main player-losses-main
                    :net-victories net-victories
                    :net-victories-main net-victories-main
                    :points (cond
                              (and (>= net-victories-main 3) (>= net-victories 3)) 1.5
                              (>= net-victories 1) 1
                              (= net-victories 0) 0.5 ;; draw
                              :else 0)}))))))

(defn sum-of [seq selector-fn]
  (->> seq (map selector-fn) (apply +)))

(defn analyze-session [session-sets]
  (let [session-players (->> session-sets
                             (mapcat (fn [[a b]] [(:name a) (:name b)]))
                             set, sort)]
    {:players (->> session-players
                   (mapv (fn [player]
                           (let [matches (collect-player-victories session-sets player session-players)]
                             {:name player
                              :matches matches
                              :victories (sum-of matches :victories)
                              :losses (sum-of matches :losses)
                              :points (sum-of matches :points)
                              }))))
     :include-in-year-points? (= (set players) (set session-players)) ;; all-players-present?
     :sets session-sets}))

(defn cumulative-scores-over-time [year-data]
  "Calculate cumulative scores for each player over time for charting"
  (let [amended-sessions (->> year-data (map-vals analyze-session))
        ;; Get sessions sorted by date, only including sessions with all players
        sorted-sessions (->> amended-sessions
                            (filter (fn [[_ session]] (:include-in-year-points? session)))
                            (sort-by first))]
    (->> players
         (map (fn [player-name]
                (let [cumulative-data
                      (->> sorted-sessions
                           (reduce (fn [acc [date session]]
                                     (let [player-data (->> (:players session)
                                                           (filter (attr= :name player-name))
                                                           first)
                                           player-points (or (:points player-data) 0)
                                           prev-total (or (:cumulative (last acc)) 0)
                                           new-total (+ prev-total player-points)]
                                       (conj acc {:date (name date)
                                                 :points player-points
                                                 :cumulative new-total})))
                                   []))]
                  {:name player-name
                   :data cumulative-data})))
         vec)))

(defn year-summary [year-data]
  (let [amended-sessions (->> year-data (map-vals analyze-session))]
    {:sessions amended-sessions
     :players (->> players
                   (mapv (fn [name]
                           {:name name
                            :points (->> amended-sessions, vals
                                         ;; Skip sessions which don't have all players
                                         (filter :include-in-year-points?)
                                         (mapcat :players)
                                         (filter (attr= :name name))
                                         (map :points)
                                         (apply +))})))}))