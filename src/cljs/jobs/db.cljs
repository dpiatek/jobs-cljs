(ns jobs.db)

(def default-db
  {:jobs {
          1 {:id       1
             :company  "exoscale"
             :title    "sre"
             :keywords ["kubernetes" "linux"]}
          2 {:id       2
             :company  "exoscale"
             :title    "sysdev"
             :keywords ["c" "clojure"]}}})
