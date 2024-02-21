(ns sports.year
  (:require [re-frame.core :as rf]
            [cljs.pprint :refer [pprint]]
            [sports.log :as log]
            [sports.util :as util]))

(defn view []
  (let [year-data @(rf/subscribe [:year-data])
        players @(rf/subscribe [:players])]
    [:div
     [:div.grid {:style {:grid-template-columns "100px 100px 100px 100px"}}
      [:div "Date"]
      (into [:<>]
            (for [p players] [:div p]))

      ]
     [:div [:button.navigation "Add Session"]]
     [:textarea {:rows 10, :cols 80, :value (util/pprint-str year-data)} ]
     ]))