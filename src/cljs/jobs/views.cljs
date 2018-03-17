(ns jobs.views
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [jobs.routes :refer [url-for]]
            [jobs.subs :as subs]))

(defn jobs-list []
  (let [jobs (re-frame/subscribe [:jobs])]
    (fn []
      [:ul
        (for [job (seq @jobs) :let [id (:id (last job))]]
          ^{:key id} [:li
                      [:a {:href (url-for :show :id id)} "Show job" id]])])))

(defn show [params]
  (let [jobs (re-frame/subscribe [:jobs])]
    (fn []
      [:div
        [:div (:id params)]
        [:a {:href (url-for :list)} "Back to listing"]])))

(defn edit []
  (fn []
    [:a {:href (url-for :list)} "Back to listing"]))

(defmulti routes identity)
(defmethod routes :show [_ params] [show params])
(defmethod routes :edit [_ params] [edit params])
(defmethod routes :default [] [jobs-list])

(defn main []
  (let [active-route (re-frame/subscribe [:active-route])]
    (fn []
      (let [{:keys [handler route-params]} @active-route]
        [routes handler route-params]))))

(defn top []
  (let [jobs-service-status  (re-frame.core/subscribe [:jobs-service-status])]
    (case @jobs-service-status
      :ok [main]
      :loading [:div "Loading jobs ..."]
      :error [:div "Error loading jobs."])))
