(ns jobs.subs
  (:require-macros [reagent.ratom :refer [reaction]])
  (:require [re-frame.core :as re-frame]))

(re-frame/reg-sub-raw
 :active-route
 (fn [db _]
   (reaction (:active-route @db))))

(re-frame/reg-sub
  :jobs
  (fn [db _]
    (:jobs db)))

(re-frame/reg-sub
  :jobs-service-status
  (fn [db _]
    (:jobs-service-status db)))

(re-frame/reg-sub
  :notification
  (fn [db _]
    (:notification db)))

(re-frame/reg-sub
  :edit-form
  (fn [db [_ label]]
    (label (:job-form db))))

(re-frame/reg-sub
  :field-errors
  (fn [db [_ label]]
    (label (:field-errors db))))

