(ns sports.aws
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [cljs.pprint :refer [pprint]]
            [sports.log :as log]))

(def api-key "Z5uRW1PQUV5HkSZMpA6Cc4wweeqZsCRaaA4hihY2")

(def url-base "https://q7hoa1mbwg.execute-api.eu-west-1.amazonaws.com/prod")

(rf/reg-event-fx ::post
  (fn [_ [_ method data response-dispatch]]
    {:http-xhrio {:method :post
                  :uri (str url-base "/" method)
                  :headers {:x-api-key api-key}
                  :params data
                  :timeout 40000 ; AWS lambdas can take 13 sec to start up
                  :format (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success response-dispatch
                  :on-failure [:aws-failure]}}))

(rf/reg-event-fx :aws-failure
  (fn [{db :db} [_ data]]
    (pprint data)
    {:db (assoc db :status (str "AWS Error: " (:debug-message data)))}))

(def filename "badminton.json")

(rf/reg-event-fx ::load-data [log/intercept]
  (fn [{db :db} _]
    {:db (assoc db :status "Loading...")
     :dispatch [::post "download"
                {:file filename}
                [::items-downloaded]]}))

(rf/reg-event-db ::items-downloaded [log/intercept]
  (fn [db [_ {items :body}]]
    (assoc db :years items
              :status nil)))

(rf/reg-event-fx ::save [log/intercept]
  (fn [{{:keys [years] :as db} :db} _]
    {:db (assoc db :status "Saving...")
     :dispatch [::post "upload"
                {:file filename
                 :content years}
                [:upload-data-success]]}))

(rf/reg-event-fx :upload-data-success [log/intercept]
  (fn [{:keys [db]} [_ {:keys [errorMessage] :as response}]]
    (println "Data Upload Finished")
    (println response)
    {:db (assoc db
           :status (or errorMessage "Data Upload Success"))}))
