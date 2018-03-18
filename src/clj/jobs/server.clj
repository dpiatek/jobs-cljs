(ns jobs.server
  (:require [org.httpkit.server    :as http]
            [cheshire.core         :as json]
            [clojure.spec.alpha    :as s]
            [bidi.bidi             :as bidi]
            [bidi.ring             :as ring]
            [clojure.java.io       :as io]
            [clojure.tools.logging :refer [info error]])
  (:gen-class))

(s/def ::title    string?)
(s/def ::company  string?)
(s/def ::keyword  string?)
(s/def ::keywords (s/coll-of ::keyword))
(s/def ::id       pos-int?)
(s/def ::id-str   (and string? (partial re-matches #"\d+")))
(s/def ::job      (s/keys :req-un [::id ::title ::company ::keywords]))
(s/def ::jobs     (s/map-of ::id ::job))

(def default-jobs
  "An example list of jobs"
  {1 {:id       1
      :company  "exoscale"
      :title    "sre"
      :keywords ["kubernetes" "linux"]}
   2 {:id       2
      :company  "exoscale"
      :title    "sysdev"
      :keywords ["c" "clojure"]}})


(defonce jobid (atom 2))
(defonce state (atom default-jobs))
(def     ->vec #(if (string? %) [%] (vec %)))

(def router
  "HTTP routes for our minimalistic API"
  ["/" {:get (ring/redirect "/index.html")
        "jobs" {:get  :list-jobs
                 :post :create-job
                 "/"   {:get           :list-jobs
                        [[#"\d+" :id]] {:delete :remove-job
                                        :get    :get-job
                                        :put    :update-job}}}
        "static" (ring/resources-maybe {:prefix "public/"})}])

(defn find-job
  [{{:keys [id]} :route-params}]
  (or (get @state (Long/parseLong (s/assert ::id-str id)))
      (throw (ex-info "can not find job in database" {::status-code 404}))))

(defmulti dispatch! :handler)

(defmethod dispatch! :list-jobs
  [_]
  {:jobs @state})

(defmethod dispatch! :get-job
  [req]
  {:job (find-job req)})

(defmethod dispatch! :create-job
  [{:keys [body] :as req}]
  (let [id    (swap! jobid inc)
        data (json/parse-stream (clojure.java.io/reader body) #(keyword %))
        job   {:id       id
               :title    (:title data)
               :company  (:company data)
               :keywords (->vec (:keywords data))}]
    (with-meta
      {:job (get (swap! state assoc id job) id)}
      {::status-code 201})))

(defmethod dispatch! :remove-job
  [req]
  (let [{:keys [id]} (find-job req)]
    (with-meta {:jobs (swap! state dissoc id)} {::status-code 204})))

(defmethod dispatch! :update-job
  [{:keys [params] :as req}]
  (let [prev    (find-job req)
        id      (:id prev)
        updates {:company  (:company params)
                 :title    (:title params)
                 :keywords (:keywords params)}]
    {:jobs (get (swap! state update id merge updates) id)}))

(defmethod dispatch! :default
  [_]
  (with-meta {:message "bad request"} {::status-code 400}))

(defn json-response
  ([payload status-code]
   (let [json-payload (json/generate-string payload)]
     {:status  status-code
      :headers {"Content-Type"   "application/json"
                "Connection"     "close"
                "Content-Length" (count json-payload)}
      :body    json-payload}))
  ([payload]
   (json-response
    payload
    (or (::status-code (meta payload)) 200))))

(defn resource-response
  [uri]
  (if-let [res (io/resource (str "public/" (or uri "index.html")))]
    {:status  200
     :headers {"Content-Type" "text/html"
               "Connection"   "close"}
     :body    (slurp res)}
    {:status  404
     :headers {"Connection" "close"}
     :body    ""}))

(defn handler
  [{:keys [uri] :as req}]
  (try
    (info "new request:" (:request-method req) (:uri req))
    (if-let [api-req (bidi/match-route* router uri req)]
      (-> api-req
          (dispatch!)
          (json-response))
      (resource-response uri))
    (catch Exception e
      (error e "error while handling request")
      (json-response {:message (.getMessage e)}
                     (or (::status-code (ex-data e)) 500)))))

(defn -main
  [& args]
  (s/check-asserts true)
  (set-validator! state #(s/assert ::jobs %))
  (http/run-server handler {:port 3000})
  (info "server started"))
