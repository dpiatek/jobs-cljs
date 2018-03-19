(ns jobs.routes
  (:require [re-frame.core :as re-frame]
            [bidi.bidi     :as bidi]
            [pushy.core    :as pushy]))

(def routes     ["/" {""                     :list
                       "new"                 :new
                      ["show/" [#"\d+" :id]] :show
                      ["edit/" [#"\d+" :id]] :edit}])

(defn- parse-url [url]
  (bidi/match-route routes url))

(defn- dispatch-route [matched-route]
  (re-frame/dispatch [:set-active-route matched-route]))

(def history (pushy/pushy dispatch-route parse-url))

(defn app-routes []
  (pushy/start! history))

(def url-for (partial bidi/path-for routes))
