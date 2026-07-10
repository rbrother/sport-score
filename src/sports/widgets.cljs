(ns sports.widgets
  (:require [sports.routes :as routes]))

(defn header
  "Sticky app bar with optional back button, title (+ optional subtitle) and
   an optional action element (e.g. a button) shown on the right."
  [{:keys [title subtitle back-url action]}]
  [:div.app-bar
   (if back-url
     [:button.back-btn {:on-click #(routes/navigate! back-url)
                         :aria-label "Back"} "‹"]
     [:span {:style {:width "8px"}}])
   [:div.app-bar-title title
    (when subtitle [:span.sub subtitle])]
   (when action [:div.app-bar-action action])])
