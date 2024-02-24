(ns sports.calculations
  (:require [medley.core :refer [map-vals]]
            [clojure.pprint :refer [pprint]]
            [sports.util :refer [attr=]]))

(def players ["Roope", "Kari", "Niklas"])

(defn count-victories [sets player opponent]
  (->> sets
       (filter (fn [[{winner-name :name} {loser-name :name}]]
                 (and (= winner-name player)
                      (= loser-name opponent))))
       count))

(defn collect-player-victories [sets player session-payers]
  (let [opponents (disj (set session-payers) player)]
    (->> opponents, sort
         (mapv (fn [opp]
                 (let [player-victories (count-victories sets player opp)
                       player-losses (count-victories sets opp player)
                       net-victories (- player-victories player-losses)]
                   {:opponent opp
                    :victories player-victories
                    :losses player-losses
                    :net-victories net-victories
                    :points (cond
                              (>= net-victories 3) 1.5
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