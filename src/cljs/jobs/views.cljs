(ns jobs.views
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [jobs.routes :refer [url-for]]
            [jobs.subs :as subs]))

(defn job-li [{:keys [id title company keywords]}]
  ^{:key id}
  [:li.jobs-li
    [:a.jobs-title {:href (url-for :show :id id)} title]
    [:div.jobs-company company]
    [:div.jobs-keywords
      (str/join ", " keywords)]])

(defn jobs []
  (let [jobs (re-frame/subscribe [:jobs])]
    (fn []
      [:div
        [:h1.title "Jobs"]
        [:a.btn {:href (url-for :new)} "Add job"]
        [:ul.jobs-list
          (for [job (seq @jobs)]
            (job-li (last job)))]])))

(defn show [params]
  (let [id (:id params)
        jobs (re-frame/subscribe [:jobs])
        {:keys [title company keywords]} (get @jobs (keyword id))]
    [:div.job-container
      [:h1.title "Job"]
      [:div.jobs-title.ut-no-link title]
      [:div.jobs-company company]
      [:div.jobs-keywords (str/join ", " keywords)]
      [:div.control-container
        [:button.btn.ut-margin-right-sm.th-secondary-btn {:type "button" :on-click #(re-frame/dispatch [:edit-job id])} "Edit job"]
        [:button.btn.ut-margin-right-sm.th-danger-btn {:type "button" :on-click #(re-frame/dispatch [:delete-job id])} "Delete job"]
        [:a.back-to-listing {:href (url-for :list)} "Back to listing"]]]))

(defn text-field [label]
  (let [val (re-frame/subscribe [:edit-form label])
        evt #(re-frame/dispatch [:update-job-form label (-> % .-target .-value)])
        field-name (str/capitalize (name label))
        field-error @(re-frame/subscribe [:field-errors label])]
    [:label.text-field
      field-name
      [:input {:value @val :type "text" :on-change evt :required true}]
      (if field-error
        [:div.text-field-error (str field-name " is required")])]))

(defn keyword-handler [event]
  (if (= (-> event .-key) "Enter")
    (do
      (re-frame/dispatch [:add-job-keyword (-> event .-target .-value)])
      (set! (-> event .-target .-value) ""))))

(defn keyword-field []
  (let [field-error @(re-frame/subscribe [:field-errors :keywords])]
    [:label.text-field
      "Keywords"
      [:input {:type "text" :on-key-down keyword-handler :placeholder "Press Enter to add"}]
      (if field-error
        [:div.text-field-error "Keywords are required"])]))

(defn keywords-list []
  (let [val (re-frame/subscribe [:edit-form :keywords])]
    [:ul.keywords-list
      (for [kw @val]
        ^{:key kw}
        [:li.keywords-li
          [:label
            kw
            [:button {:type "button"
                      :on-click #(re-frame/dispatch [:delete-job-keyword kw])} "×"]]])]))

(defn edit [{:keys [id]}]
  (fn []
    [:div
      [:h1.title "Edit job"]
      [:form.form-container
        [text-field :title]
        [text-field :company]
        [keyword-field]
        [keywords-list]
        [:div.control-container
          [:button.btn {:type "button" :on-click #(re-frame/dispatch [:submit-job-update id])} "Submit"]
          [:a.back-to-listing {:href (url-for :list)} "Back to listing"]]]]))

(defn new []
  (fn []
    [:div
      [:h1.title "Add job"]
      [:form.form-container
        [text-field :title]
        [text-field :company]
        [keyword-field]
        [keywords-list]
        [:div.control-container
          [:button.btn {:type "button" :on-click #(re-frame/dispatch [:submit-new-job])} "Submit"]
          [:a.back-to-listing {:href (url-for :list)} "Back to listing"]]]]))

(defmulti routes identity)
(defmethod routes :show [_ params] [show params])
(defmethod routes :edit [_ params] [edit params])
(defmethod routes :new [_ params] (do (re-frame/dispatch [:reset-form]) [new params]))
(defmethod routes :default [] [jobs])

(defn notification []
  (let [notification @(re-frame/subscribe [:notification])
        notification-css (case (:state notification)
                           :error :div.notification.notification-error
                           :div.notification)]
    (if-not (empty? notification)
      [notification-css (:msg notification) [:span {:on-click #(re-frame/dispatch [:clear-notification])} "×"]])))

(defn main []
  (let [active-route (re-frame/subscribe [:active-route])]
    (fn []
      (let [{:keys [handler route-params]} @active-route]
        [:div.container
          [notification]
          [routes handler route-params]]))))

(defn top []
  (let [jobs-service-status  (re-frame.core/subscribe [:jobs-service-status])
        notification @(re-frame/subscribe [:notification])]
    (case @jobs-service-status
      :ok [main]
      :loading [:div.notification "Loading jobs ..."]
      :error [:div.notification.notification-error (:msg notification)])))
