(ns sports.core
  (:require
   [reagent.dom :as rdom]
   [re-frame.core :as re-frame]
   [reagent-dev-tools.core :as dev-tools]
   [sports.styles]
   [sports.subs]
   [sports.main :as main]))

(defn get-element-by-id [id] (.getElementById js/document id))

(defn ^:dev/after-load mount-root []
  (re-frame/clear-subscription-cache!)
  (let [root-el (get-element-by-id "app")]
    (rdom/unmount-component-at-node root-el)
    (rdom/render [main/main-panel] root-el)))

(defn init []
  (re-frame/dispatch-sync [::main/initialize-db])
  (dev-tools/start! {:state-atom re-frame.db/app-db})
  (mount-root))

