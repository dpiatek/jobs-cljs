(ns jobs.events
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
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
  :update-job-form
  (fn [{:keys [job-form] :as db} [_ label val]]
    (assoc db :job-form (assoc job-form label val))))

(re-frame/reg-event-db
  :reset-job-service-status
  (fn [db _]
    (assoc db :job-service-status :init)))

(re-frame/reg-event-fx
  :fetch-jobs
  (fn
    [{db :db} _]

    {:http-xhrio {:method          :get
                  :uri             "/jobs"
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-jobs-response]
                  :on-failure      [:failed-jobs-response]}
     :db  (assoc db :jobs-service-status :loading)}))

(re-frame/reg-event-db
  :process-jobs-response
  (fn
    [db [_ {:keys [jobs]} response]]
    (-> db
        (assoc :jobs-service-status :ok)
        (assoc :jobs jobs))))

(re-frame/reg-event-db
  :failed-jobs-response
  (fn
    [db [_ {:keys [jobs]} response]]
    (-> db
        (assoc :jobs-service-status :error))))

(re-frame/reg-event-fx
  :submit-new-job
  (fn
    [{db :db} _]
    {:http-xhrio {:method          :post
                  :uri             "/jobs"
                  :params          (:job-form db)
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-job-response]
                  :on-failure      [:failed-job-response]}
     :db  (assoc db :job-service-status :loading)}))

(re-frame/reg-event-db
  :process-job-response
  (fn
    [db [_ {:keys [job]} response]]
    (-> db
        (assoc :job-service-status :ok)
        (assoc :job-form {})
        (assoc-in [:jobs (:id job)] job))))

(re-frame/reg-event-db
  :failed-job-response
  (fn
    [db [_ {:keys [jobs]} response]]
    (-> db
        (assoc :job-service-status :error))))
