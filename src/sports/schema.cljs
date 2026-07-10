(ns sports.schema
  "Malli schemas describing the shape of app-db, plus a global re-frame
   interceptor that validates app-db after every event, so that bugs which
   corrupt app-db are caught immediately (with a clear error) instead of
   causing confusing failures later on."
  (:require [malli.core :as m]
            [malli.error :as me]
            [reagent.core :as reagent]
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

;; Plain reagent atom (deliberately kept outside app-db) holding a human-readable
;; description of the last app-db validation failure, or nil when app-db is valid.
;; A UI banner (see sports.main) derefs this atom to warn the user in-app,
;; in addition to the details already logged to the console.
(defonce validation-error (reagent/atom nil))

(defn validate!
  "Checks db against the AppDb schema and updates validation-error
   accordingly (logging details to the console when invalid).
   Returns true when db is valid, false otherwise."
  [db event]
  (if (m/validate AppDb db)
    (do (reset! validation-error nil) true)
    (let [errors (explain-str AppDb db)
          message (str "Invalid app-db after event " (pr-str event) ": " errors)]
      (log/log :error message)
      (reset! validation-error message)
      false)))

(def check-schema
  "Global interceptor: validates the resulting app-db after every event.
   If the event's :db effect would produce an invalid app-db, that effect is
   discarded (app-db is left unchanged) so invalid data can never be
   committed - the validation-error banner still informs the user."
  (rf/->interceptor
    :id ::check-schema
    :after (fn [context]
             (let [event (get-in context [:coeffects :event])
                   prev-db (get-in context [:coeffects :db])]
               (if (contains? (:effects context) :db)
                 (let [new-db (get-in context [:effects :db])]
                   (if (validate! new-db event)
                     context
                     (assoc-in context [:effects :db] prev-db)))
                 context)))))

(rf/reg-global-interceptor check-schema)
