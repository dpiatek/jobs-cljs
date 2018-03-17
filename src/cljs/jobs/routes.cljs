(ns jobs.routes
  (:require [re-frame.core :as re-frame]
            [jobs.events   :as events]
            [bidi.bidi     :as bidi]
            [pushy.core    :as pushy]))

(def routes     ["/" {""                     :list
                      ["show/" [#"\d+" :id]] :show
                      ["edit/" [#"\d+" :id]] :edit}])

(def url-for    (partial bidi/path-for routes))
