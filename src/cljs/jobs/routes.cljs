(ns jobs.routes
  (:require [re-frame.core :as re-frame]
            [jobs.events   :as events]
            [bidi.bidi     :as bidi]
            [pushy.core    :as pushy]))

(def routes     ["/" {""                     :list
                      ["show/" [#"\d+" :id]] :show
                      ["edit/" [#"\d+" :id]] :edit}])

(defn- parse-url [url]
  (bidi/match-route routes url))

(defn- dispatch-route [matched-route]
  (re-frame/dispatch [:set-active-route matched-route]))

(defn app-routes []
  (pushy/start! (pushy/pushy dispatch-route parse-url)))

(def url-for (partial bidi/path-for routes))
