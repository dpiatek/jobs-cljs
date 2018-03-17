(ns jobs.events
  (:require [re-frame.core :as re-frame]
            [ajax.core     :as ajax]
            [jobs.db       :as db]))

(re-frame/reg-event-db
 :initialize-db
 (fn  [_ _]
   db/default-db))

(re-frame/reg-event-db
 :set-active-route
 (fn [db [_ active-route]]
   (assoc db :active-route active-route)))