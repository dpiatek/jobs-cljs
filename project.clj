(defproject jobs "0.1.0"
  :dependencies [[org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.9.946"]
                 [org.clojure/core.async "0.4.474"]
                 [org.clojure/tools.logging "0.4.0"]
                 [spootnik/unilog "0.7.22"]
                 [http-kit "2.2.0"]
                 [reagent "0.8.0-alpha2"]
                 [re-frame "0.10.3-rc2"]
                 [day8.re-frame/http-fx "0.1.4"]
                 [cheshire "5.8.0"]
                 [bidi     "2.1.3"]
                 [kibu/pushy "0.3.8"]]

  :plugins [[lein-cljsbuild "1.1.5"]]

  :min-lein-version "2.5.3"

  :source-paths ["src/clj"]

  :clean-targets ^{:protect false} ["resources/public/js/compiled" "target"]

  :figwheel {:css-dirs ["resources/public/css"]
             :ring-handler jobs.server/handler}

  :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

  :profiles
  {:dev
   {:dependencies [[binaryage/devtools "0.9.4"]
                   [figwheel-sidecar "0.5.13"]
                   [com.cemerick/piggieback "0.2.2"]]

    :plugins      [[lein-figwheel "0.5.13"]]}}

  :cljsbuild
  {:builds
   [{:id           "dev"
     :source-paths ["src/cljs"]
     :figwheel     {:on-jsload "jobs.core/mount-root"}
     :compiler     {:main                 jobs.core
                    :output-to            "resources/public/js/compiled/app.js"
                    :output-dir           "resources/public/js/compiled/out"
                    :asset-path           "js/compiled/out"
                    :source-map-timestamp true
                    :preloads             [devtools.preload]
                    :external-config      {:devtools/config {:features-to-install :all}}
                    }}

    {:id           "min"
     :source-paths ["src/cljs"]
     :jar true
     :compiler     {:main            jobs.core
                    :output-to       "resources/public/js/compiled/app.js"
                    :optimizations   :advanced
                    :closure-defines {goog.DEBUG false}
                    :pretty-print    false}}


    ]}

  :main jobs.server

  :aot [jobs.server]

  :uberjar-name "jobs.jar"

  :prep-tasks [["cljsbuild" "once" "min"] "compile"]
  )
