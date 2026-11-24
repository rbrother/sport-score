(ns sports.core
  (:require
    [reagent.dom.client :as rdom]
    [re-frame.core :as re-frame]
    [reagent-dev-tools.core :as dev-tools]
    [sports.styles]
    [sports.subs]
    [sports.schema]
    [sports.routes :as routes]
    [sports.main :as main]))

(defn get-element-by-id [id] (.getElementById js/document id))

(defonce root (atom nil))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (when-not @root
    (reset! root (rdom/create-root (get-element-by-id "app"))))
  (rdom/render @root [main/main-panel]))

(defn init []
  (re-frame/dispatch-sync [::main/initialize-db])
  (routes/init-routes!)
  (when ^boolean js/goog.DEBUG
    (dev-tools/start! {:state-atom re-frame.db/app-db}))
  (mount-root))
