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
  (fn [{job-form :job-form :as db} [_ label val]]
    (assoc db :job-form (assoc job-form label val))))

(defn add-uniq [keywords val]
  (vec (conj (set keywords) val)))

(re-frame/reg-event-db
  :add-job-keyword
  (fn [{job-form :job-form :as db} [_ val]]
    (assoc db :job-form
      (assoc job-form :keywords (add-uniq (:keywords job-form) val)))))

(re-frame/reg-event-db
  :delete-job-keyword
  (fn [{job-form :job-form :as db} [_ val]]
    (assoc db :job-form
      (assoc job-form :keywords (remove #{val} (:keywords job-form))))))

(re-frame/reg-event-fx
  :fetch-jobs
  (fn [{db :db} _]
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
    [db [_ {jobs :jobs}]]
    (-> db
      (assoc :jobs-service-status :ok)
      (assoc :jobs jobs))))

(re-frame/reg-event-db
  :failed-jobs-response
  (fn
    [db [_ _]]
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
                  :on-failure      [:failed-job-response]}}))

(re-frame/reg-event-db
  :process-job-response
  (fn
    [db [_ {job :job}]]
    (-> db
      (assoc :job-form {})
      (assoc-in [:jobs (keyword (str (:id job)))] job))))

(re-frame/reg-event-db
  :failed-job-response
  (fn
    [db [_ _]]
    db))

(re-frame/reg-event-fx
  :delete-job
  (fn
    [{db :db} [_ id]]
    {:http-xhrio {:method          :delete
                  :uri             (str "/jobs/" id)
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-delete-response id]
                  :on-failure      [:failed-delete-response]}}))

(re-frame/reg-event-db
  :process-delete-response
  (fn
    [{jobs :jobs :as db} [_ id response]]
    (-> db
      (assoc :jobs (dissoc jobs (keyword id))))))

(re-frame/reg-event-db
  :failed-delete-response
  (fn
    [db [_ _]]
    db))

(re-frame/reg-event-fx
  :edit-job
  (fn [{db :db} [_ id]]
    {:db (assoc db :job-form (get (:jobs db) (keyword id)))
     :dispatch [:set-active-route {:route-params {:id id}, :handler :edit}]}))

(re-frame/reg-event-fx
  :submit-job-update
  (fn
    [{db :db} [_ id]]
    {:http-xhrio {:method          :put
                  :uri             (str "/jobs/" id)
                  :params          (:job-form db)
                  :format          (ajax/json-request-format)
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [:process-update-response]
                  :on-failure      [:failed-update-response]}}))

(re-frame/reg-event-db
  :process-update-response
  (fn
    [db [_ job]]
    (-> db
      (assoc :jobs (merge (:jobs db) job))
      (assoc :job-form {}))))

(re-frame/reg-event-db
  :failed-update-response
  (fn
    [db [_ _]]
    db))

(re-frame/reg-event-db
  :reset-form
  (fn
    [db [_ _]]
    (assoc db :job-form {})))
