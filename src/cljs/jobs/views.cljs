(ns jobs.views
  (:require [re-frame.core :as re-frame]
            [clojure.string :as str]
            [jobs.routes :refer [url-for]]
            [jobs.subs :as subs]))

(defn main-panel []
  [:div [:p "hello"]])
