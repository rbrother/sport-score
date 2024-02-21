(ns sports.aws
  (:require [re-frame.core :as rf]
            [ajax.core :as ajax]
            [day8.re-frame.http-fx]
            [cljs.pprint :refer [pprint]]))

(def api-key "Z5uRW1PQUV5HkSZMpA6Cc4wweeqZsCRaaA4hihY2")

(def url-base "https://q7hoa1mbwg.execute-api.eu-west-1.amazonaws.com/prod")

(rf/reg-event-fx :aws-post
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