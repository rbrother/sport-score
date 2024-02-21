(ns sports.main
  (:require-macros [reagent-mui.util :refer [react-component]])
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [medley.core :refer [map-vals]]
            ["@mui/material/styles" :refer [ThemeProvider createTheme]]
            [sports.util :as util]
            [sports.aws]
            [sports.widgets :as widgets]))

(def theme-provider (r/adapt-react-class ThemeProvider))

;; VIEWS

(def pages [{:id :summary :label "Summary of Expenses"}
            {:id :list :label "List of Items"}
            {:id :import :label "Import new Items"}])

(defn page-selection-button [{:keys [id label]}]
  (let [current-page @(rf/subscribe [:page])
        on-click #(rf/dispatch [::navigate-to-page id])
        style (if (= current-page id) {:background "#FFF"} {})]
    [:button.navigation {:on-click on-click :style style} label]))

(defn page-selection-bar []
  (into [:div] (map page-selection-button pages)))

(def dark-theme
  (createTheme (clj->js {:palette {:mode "dark"}})))

(defn main-panel []
  (let [page @(rf/subscribe [:page])]
    [theme-provider {:theme dark-theme}
     [:div
      [:h2 "Sport Tracker"]
      #_(case page
        :summary [summary-table/view]
        :list [item-table/view]
        :import [import-items/view])]]))

;; SUBS

(rf/reg-sub :navigation (fn [db _] (:navigation db)))

(rf/reg-sub :page :<- [:navigation] (fn [nav _] (:page nav)))

;; EVENTS

(rf/reg-event-fx ::initialize-db
                 (fn [_ _]
                   {:db {:new-items-type :s-bank-account-csv
                         :items-index {}
                         :navigation {:page :summary
                                      :start-month "2023-01"}}
                    :dispatch [:aws-post "download"
                               {:file "accounting.json"}
                               [::items-downloaded]]}))

(rf/reg-event-db ::items-downloaded
                 (fn [db [_ {items :body}]]
                   (assoc db :items-index (util/index-by-id items))))

(rf/reg-event-fx :upload-data-success
                 (fn [{:keys [db]} [_ {:keys [errorMessage] :as response}]]
                   (println "Data Upload Finished")
                   (println response)
                   {:db (assoc db
                          :status (or errorMessage "Data Upload Success"))
                    :dispatch [::navigate-to-page :list]}))
