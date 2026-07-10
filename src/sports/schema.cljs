(ns sports.schema
  "Malli schemas describing the shape of app-db, plus a global re-frame
   interceptor that validates app-db after every event, so that bugs which
   corrupt app-db are caught immediately (with a clear error) instead of
   causing confusing failures later on."
  (:require [malli.core :as m]
            [malli.error :as me]
            [re-frame.core :as rf]
            [sports.log :as log]))

;; A single player's outcome within one set, e.g. {:name "Roope" :score 11}
;; Name/score may be nil while a set is still being edited (see add-set.cljs).
;; :score may be entirely absent from stored data when a loser scored 0 points.
(def SetPlayer
  [:map {:closed true}
   [:name [:maybe string?]]
   [:score {:optional true} [:maybe int?]]])

;; A set is a pair of [winner loser] player results.
(def SetPair [:tuple SetPlayer SetPlayer])

;; All the sets played during one session (one day).
(def SessionSets [:vector SetPair])

;; Sessions of one year, keyed by session date, e.g. :2024-05-20
(def Year [:map-of keyword? SessionSets])

;; All years, keyed by year id, e.g. :2023 or :2013K
(def Years [:map-of keyword? Year])

(def Page
  [:enum :years :year :session :add-set :add-session :session-raw-data])

(def Navigation
  [:map {:closed true}
   [:page Page]
   [:year {:optional true} [:maybe keyword?]]
   [:session {:optional true} [:maybe keyword?]]])

(def AppDb
  [:map {:closed true}
   [:years Years]
   [:navigation Navigation]
   [:status {:optional true} [:maybe string?]]])

(defn explain-str [schema value]
  (-> (m/explain schema value) me/humanize pr-str))

(defn validate!
  "Throws a descriptive error if db does not conform to AppDb schema."
  [db event]
  (when-not (m/validate AppDb db)
    (let [message (str "Invalid app-db after event " (pr-str event) ": "
                        (explain-str AppDb db))]
      (log/log :error message)
      (throw (ex-info message {:event event :errors (explain-str AppDb db) :db db})))))

(def check-schema
  "Global interceptor: validates the resulting app-db after every event."
  (rf/->interceptor
    :id ::check-schema
    :after (fn [context]
             (let [event (get-in context [:coeffects :event])
                   db (if (contains? (:effects context) :db)
                        (get-in context [:effects :db])
                        (get-in context [:coeffects :db]))]
               (validate! db event))
             context)))

(rf/reg-global-interceptor check-schema)
