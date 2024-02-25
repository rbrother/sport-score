(ns sports.import
  (:require [clojure.string :as str]
            [sports.util :as util]))

(defn- import-set [[roope niklas kari _]]
  (->> [{:name "Roope", :score (util/try-parse-int roope)}
        {:name "Kari", :score (util/try-parse-int kari)}
        {:name "Niklas", :score (util/try-parse-int niklas)}]
       (filter :score)
       (sort-by #(- (:score %)))
       vec))

(defn pre-sets [[niklas kari]]
  (concat (repeat (util/try-parse-int niklas)
                  [{:name "Niklas", :score 11} {:name "Kari"}])
          (repeat (util/try-parse-int kari)
                  [{:name "Kari", :score 11} {:name "Niklas"}])))

(defn import-csv-session [s]
  (let [vals (str/split s #",")
        date (keyword (get vals 2))
        pre-nk (take 2 (drop 16 vals))
        sets-data (partition 4 (drop 20 vals))]
    [date (vec (concat (pre-sets pre-nk)
                       (map import-set sets-data)))]))

(defn year-data-from-csv [csv]
  (->> (str/split csv #"\n")
       (map import-csv-session)
       (into {})))