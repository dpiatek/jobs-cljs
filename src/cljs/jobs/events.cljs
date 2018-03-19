(ns jobs.events
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [clojure.spec.alpha :as s]
            [ajax.core     :as ajax]
            [jobs.db       :as db]
            [jobs.routes   :as routes]
            [day8.re-frame.http-fx]
            [pushy.core    :as pushy]))

(s/def ::title    (and string? (complement str/blank?)))
(s/def ::company  (and string? (complement str/blank?)))
(s/def ::keyword  string?)
(s/def ::keywords (s/coll-of ::keyword))
(s/def ::id       pos-int?)
(s/def ::id-str   (and string? (partial re-matches #"\d+")))
(s/def ::job      (s/keys :req-un [::id ::title ::company ::keywords]))
(s/def ::new-job  (s/keys :req-un [::title ::company ::keywords]))

(defn default-job []
  {:title "" :company "" :keywords nil})

(defn field-errors [db spec]
  (into {}
    (vec
      (map #(conj (:path %) true) (:cljs.spec.alpha/problems (s/explain-data spec (merge (default-job) (:job-form db))))))))

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

(defn cleanse [keywords val]
  (filter #((complement str/blank?) %) (vec (conj (set keywords) val))))

(re-frame/reg-event-db
  :add-job-keyword
  (fn [{job-form :job-form :as db} [_ val]]
    (assoc db :job-form
      (assoc job-form :keywords (cleanse (:keywords job-form) val)))))

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
    (if (s/valid? ::new-job (:job-form db))
      {:http-xhrio {:method          :post
                    :uri             "/jobs"
                    :params          (:job-form db)
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:process-job-response]
                    :on-failure      [:failed-job-response]}}
      {:db (assoc db :field-errors (field-errors db ::new-job))})))

(re-frame/reg-event-fx
  :process-job-response
  (fn
    [{db :db} [_ {job :job}]]
    (pushy/set-token! routes/history (routes/url-for :list))
    {:db (-> db
          (assoc :job-form {})
          (assoc :field-errors {})
          (assoc-in [:jobs (keyword (str (:id job)))] job))
     :dispatch [:set-active-route {:route-params {:id (:id job)} :handler :sho}]}))

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
    (pushy/set-token! routes/history (routes/url-for :list))
    (-> db
      (assoc :jobs (dissoc jobs (keyword id))))))

(re-frame/reg-event-fx
  :process-delete-response
  (fn
    [{db :db} [_ id]]
    (pushy/set-token! routes/history (routes/url-for :list))
    {:db (assoc db :jobs (dissoc (:jobs db) (keyword id)))
     :dispatch [:set-active-route {:handler :list}]}))

(re-frame/reg-event-db
  :failed-delete-response
  (fn
    [db [_ _]]
    db))

(re-frame/reg-event-fx
  :edit-job
  (fn [{db :db} [_ id]]
    (pushy/set-token! routes/history (routes/url-for :edit :id id))
    {:db (assoc db :job-form (get (:jobs db) (keyword id)))
     :dispatch [:set-active-route {:route-params {:id id}, :handler :edit}]}))

(re-frame/reg-event-fx
  :submit-job-update
  (fn
    [{db :db} [_ id]]
    (if (s/valid? ::job (:job-form db))
      {:http-xhrio {:method          :put
                    :uri             (str "/jobs/" id)
                    :params          (:job-form db)
                    :format          (ajax/json-request-format)
                    :response-format (ajax/json-response-format {:keywords? true})
                    :on-success      [:process-update-response]
                    :on-failure      [:failed-update-response]}}
      {:db (assoc db :field-errors (field-errors db ::job))})))

(re-frame/reg-event-fx
  :process-update-response
  (fn
    [{db :db} [_ job]]
    (pushy/set-token! routes/history (routes/url-for :list))
    {:db (-> db
          (assoc :jobs (merge (:jobs db) job))
          (assoc :job-form {})
          (assoc :field-errors {}))
     :dispatch [:set-active-route {:handler :list}]}))

(re-frame/reg-event-db
  :failed-update-response
  (fn
    [db [_ _]]
    db))

(re-frame/reg-event-db
  :reset-form
  (fn
    [db [_ _]]
    (-> db
      (assoc :job-form {}))))
