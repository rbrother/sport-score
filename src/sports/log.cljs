(ns sports.log
  (:require [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]
            [clojure.data :refer [diff]]
            [clojure.string :as string]
            [cljs-time.core :as time]
            [cljs-time.format :as time-format]
            [cljs-time.core :as time]))

(def time-formatter-long (time-format/formatter "yyyy-MM-dd hh:mm:ss"))

(defn format-time-long [t] (time-format/unparse time-formatter-long t))

(def log-colors #{:green :blue :purple})

(defn indent-lines
  ([s] (indent-lines s "        "))
  ([s indent] (->> s (string/split-lines) (map #(str indent %)) (string/join "\n"))))

(defn pprint-s
  ([s] (pprint-s s "        "))
  ([s indent]
   (binding [*print-length* 5]
     (indent-lines (with-out-str (pprint s)) indent))))

(defn log [& data]
  (let [error (= (first data) :error)
        color (get log-colors (first data))
        content (if (or error color) (rest data) data)]
    (cond
      error (.error js/console (string/join content))
      color (.log js/console (str "%c" (string/join content)) (str "color: " (name color)))
      :else (.log js/console (string/join content)))
    (first data))) ;; return first item as value, so can use in middle of expressions

(defn log-data [data]
  (log "-------------------\n" (pprint-s data))
  data)

(def intercept
  (rf/->interceptor
    :id :debug
    :before identity
    :after (fn [{:keys [coeffects effects] :as context}]
             (let [orig-db (get coeffects :db)
                   after-db (get effects :db)
                   [only-in-orig only-in-after _] (diff orig-db after-db)
                   before (str only-in-orig)
                   after (str only-in-after)]
               (log "[EVENT " (format-time-long (time/now)) "] " (str (first (:event coeffects))))
               context))))