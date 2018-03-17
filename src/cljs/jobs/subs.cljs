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
