(ns jobs.views
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [jobs.routes :refer [url-for]]
            [jobs.subs :as subs]))

(defn job-li [{:keys [id title company keywords]}]
  ^{:key id}
  [:li
    [:a {:href (url-for :show :id id)} title]
    [:div company]
    [:div
      (str/join ", " keywords)]])

(defn jobs []
  (let [jobs (re-frame/subscribe [:jobs])]
    (fn []
      [:ul
        (for [job (seq @jobs)]
          (job-li (last job)))])))

(defn show [params]
  (let [id (:id params)
        jobs (re-frame/subscribe [:jobs])
        {:keys [title company keywords]} (get @jobs (keyword id))]
    (fn []
      [:div
        [:div title]
        [:div company]
        [:div
          (str/join ", " keywords)]
        [:a {:href (url-for :list)} "Back to listing"]])))

(defn edit []
  (fn []
    [:a {:href (url-for :list)} "Back to listing"]))

(defmulti routes identity)
(defmethod routes :show [_ params] [show params])
(defmethod routes :edit [_ params] [edit params])
(defmethod routes :default [] [jobs])

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
