(ns sports.routes
  "URL-based routing for the SPA. `accountant` syncs the browser's HTML5
   History API with a tiny hand-rolled route matcher below (secretary was
   tried first, but its route-param parsing is broken on modern
   ClojureScript - it feeds plain lists into `merge-with`, which requires
   map entries - so route matching is done directly here instead).
   The :navigation key in app-db is always derived from the current URL,
   so the browser back/forward buttons work naturally."
  (:require [clojure.string :as string]
            [accountant.core :as accountant]
            [re-frame.core :as rf]))

;; URL BUILDERS - used by views to navigate instead of directly changing app-db

(defn years-url [] "/")
(defn year-url [year] (str "/year/" (name year)))
(defn year-raw-data-url [year] (str (year-url year) "/raw-data"))
(defn add-session-url [year] (str "/year/" (name year) "/add-session"))
(defn session-url [year session] (str "/year/" (name year) "/session/" (name session)))
(defn add-set-url [year session] (str (session-url year session) "/add-set"))
(defn raw-data-url [year session] (str (session-url year session) "/raw-data"))

(defn navigate! [url]
  (accountant/navigate! url))

;; re-frame effect so event handlers can trigger navigation as a side effect
(rf/reg-fx :navigate! (fn [url] (navigate! url)))

;; ROUTE MATCHING - each pattern is a vector of path segments, where a
;; segment starting with ":" names a param to be extracted from the URL.

(defn- path-segments [path]
  (->> (string/split path #"/") (remove string/blank?) vec))

(defn- parse-pattern [path]
  (mapv (fn [seg] (if (string/starts-with? seg ":") (keyword (subs seg 1)) seg))
        (path-segments path)))

(defn- match-pattern [pattern segs]
  (when (= (count pattern) (count segs))
    (loop [pattern pattern segs segs params {}]
      (if (empty? pattern)
        params
        (let [p (first pattern) s (first segs)]
          (cond
            (keyword? p) (recur (rest pattern) (rest segs) (assoc params p s))
            (= p s) (recur (rest pattern) (rest segs) params)
            :else nil))))))

;; ROUTES - each route dispatches a pure event that sets :navigation from the URL

(def ^:private route-table
  [[(parse-pattern "/")
    (fn [_] (rf/dispatch [:navigate/years]))]
   [(parse-pattern "/year/:year")
    (fn [{:keys [year]}] (rf/dispatch [:navigate/year (keyword year)]))]
   [(parse-pattern "/year/:year/raw-data")
    (fn [{:keys [year]}] (rf/dispatch [:navigate/year-raw-data (keyword year)]))]
   [(parse-pattern "/year/:year/add-session")
    (fn [{:keys [year]}] (rf/dispatch [:navigate/add-session (keyword year)]))]
   [(parse-pattern "/year/:year/session/:session")
    (fn [{:keys [year session]}] (rf/dispatch [:navigate/session (keyword year) (keyword session)]))]
   [(parse-pattern "/year/:year/session/:session/add-set")
    (fn [{:keys [year session]}] (rf/dispatch [:navigate/add-set (keyword year) (keyword session)]))]
   [(parse-pattern "/year/:year/session/:session/raw-data")
    (fn [{:keys [year session]}] (rf/dispatch [:navigate/session-raw-data (keyword year) (keyword session)]))]])

(defn- find-route [path]
  (let [path-only (first (string/split path #"\?"))
        segs (path-segments path-only)]
    (some (fn [[pattern handler]]
            (when-let [params (match-pattern pattern segs)]
              [handler params]))
          route-table)))

;; EVENTS - pure app-db updates, triggered only by route matches above

(rf/reg-event-db :navigate/years
  (fn [db _] (assoc db :navigation {:page :years})))

(rf/reg-event-db :navigate/year
  (fn [db [_ year]] (assoc db :navigation {:page :year :year year})))

(rf/reg-event-db :navigate/year-raw-data
  (fn [db [_ year]] (assoc db :navigation {:page :year-raw-data :year year})))

(rf/reg-event-db :navigate/add-session
  (fn [db [_ year]] (assoc db :navigation {:page :add-session :year year})))

(rf/reg-event-db :navigate/session
  (fn [db [_ year session]] (assoc db :navigation {:page :session :year year :session session})))

(rf/reg-event-db :navigate/add-set
  (fn [db [_ year session]] (assoc db :navigation {:page :add-set :year year :session session})))

(rf/reg-event-db :navigate/session-raw-data
  (fn [db [_ year session]] (assoc db :navigation {:page :session-raw-data :year year :session session})))

;; INIT - wire accountant to the route matcher and process the current URL on load

(defn- dispatch-path! [path]
  (when-let [[handler params] (find-route path)]
    (handler params)))

(defn init-routes! []
  (accountant/configure-navigation!
    {:nav-handler  dispatch-path!
     :path-exists? (fn [path] (boolean (find-route path)))})
  (accountant/dispatch-current!))
