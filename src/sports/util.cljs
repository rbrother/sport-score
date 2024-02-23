(ns sports.util
  (:require [cljs.pprint :refer [pprint]]))

(defn attr= [attr value] (fn [x] (= (get x attr) value)))

(defn index-by-id [list]
  (into {} (map (fn [{:keys [id] :as item}] [id item]) list)))

(def max-len 25)

(defn shorten [s] (if (< (count s) (- max-len 2))
                    s
                    (str (subs s 0 max-len) "...")))

(defn show [val] ;; Use in middle of calculation chain to show intermediate value
  (do
    (pprint val)
    val))

(defn starts-with [start str]
  (when str (= start (subs str 0 (count start)))))

(defn pprint-str [str]
  (with-out-str (pprint str)))

(defn set-local-state
  ([state path]
   (set-local-state state path identity))
  ([state path coerce-fn]
   (fn [event]
     (let [value (-> event .-target .-value)]
       (swap! state #(assoc-in % path (coerce-fn value)))))))

(defn try-parse-int [s]
  (let [parsed (js/parseInt s)]
    (cond
      (= s "") nil
      (js/isNaN parsed) s
      :else parsed)))