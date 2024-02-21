(ns sports.util
  (:require [clojure.string :refer [split split-lines]]
            [cljs.pprint :refer [pprint]]))

(defn index-by-id [list]
  (into {} (map (fn [{:keys [id] :as item}] [id item]) list)))

(def max-len 25)

(defn shorten [s] (if (< (count s) (- max-len 2))
                    s
                    (str (subs s 0 max-len) "...")))

(defn show [val]    ;; Use in middle of calculation chain to show intermediate value
  (do
    (pprint val)
    val))

(defn starts-with [start str]
  (when str (= start (subs str 0 (count start)))))

(defn pprint-str [str]
  (with-out-str (pprint str)))
