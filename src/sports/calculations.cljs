(ns sports.calculations
  (:require [medley.core :refer [map-vals]]))

(defn amend-set [[a b]]
  (let [a-winner? (> (:score a) (:score b))]
    [(assoc a :winner? a-winner?)
     (assoc b :winner? (not a-winner?))]))

(defn count-victories [sets player opponent]
  (->> sets
       (filter (fn [[a b]]
                 (or (and (= (:name a) player)
                          (= (:name b) opponent)
                          (:winner? a))
                     (and (= (:name b) player)
                          (= (:name a) opponent)
                          (:winner? b)))))
       count))

(defn collect-player-victories [sets player all-payers]
  (let [opponents (disj (set all-payers) player)]
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
                              :else 0)}))))))

(defn sum-of [seq selector-fn]
  (->> seq (map selector-fn) (apply +)))

(defn analyze-session [session-seq]
  (let [amended-sets (mapv amend-set session-seq)
        players (->> session-seq
                     (mapcat (fn [[a b]] [(:name a) (:name b)]))
                     set, sort)]
    {:players (->> players
                   (mapv (fn [player]
                           (let [matches (collect-player-victories amended-sets player players)]
                             {:name player
                              :matches matches
                              :victories (sum-of matches :victories)
                              :losses (sum-of matches :losses)
                              :points (sum-of matches :points)}))))
     :sets amended-sets}))

(defn year-summary [year-data]
  (let [amended-sessions  (->> year-data (map-vals analyze-session))]
    {:sessions amended-sessions
     ;; summary data
     }))