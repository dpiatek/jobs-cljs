(ns jobs.events
  (:require [re-frame.core :as re-frame]
            [ajax.core     :as ajax]
            [jobs.db       :as db]
            [day8.re-frame.http-fx]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-route
 (fn [db [_ active-route]]
   (assoc db :active-route active-route)))

(re-frame/reg-event-db
  :process-response
  (fn
    [db [_ {:keys [jobs]} response]]
    (-> db
        (assoc :jobs-service-status :ok)
        (assoc :jobs jobs))))

(re-frame/reg-event-db
  :failed-response
  (fn
    [db [_ {:keys [jobs]} response]]
    (-> db
        (assoc :jobs-service-status :error))))

(re-frame/reg-event-fx
  :fetch-jobs
  (fn
    [{db :db} _]

    {:http-xhrio {:method          :get
                  :uri             "/jobs"
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-response]
                  :on-failure      [:failed-response]}
     :db  (assoc db :jobs-service-status :loading)}))
