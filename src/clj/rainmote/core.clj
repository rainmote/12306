(ns rainmote.core
  (:require [rainmote.handler :as handler]
            [rainmote.nrepl :as nrepl]
            [luminus.http-server :as http]
            [luminus-migrations.core :as migrations]
            [rainmote.config :refer [env]]
            [clojure.tools.cli :refer [parse-opts]]
            [rainmote.tools.ruokuai :as ruokuai]
            [mount.core :as mount]
            [taoensso.timbre :as timbre]
            [rainmote.query-dns]
            [taoensso.timbre.appenders.core :as appenders])
  (:gen-class))

(def cli-options
  [["-p" "--port PORT" "Port number"
    :parse-fn #(Integer/parseInt %)]
   [nil "--ruokuai FILE" "Prase 12306 check code"]])

(mount/defstate ^{:on-reload :noop} http-server
  :start
  (http/start
    (-> env
        (assoc  :handler #'handler/app)
        (update :io-threads #(or % (* 2 (.availableProcessors (Runtime/getRuntime)))))
        (update :port #(or (-> env :options :port) %))))
  :stop
  (http/stop http-server))

(mount/defstate ^{:on-reload :noop} repl-server
  :start
  (when (env :nrepl-port)
    (nrepl/start {:bind (env :nrepl-bind)
                  :port (env :nrepl-port)}))
  :stop
  (when repl-server
    (nrepl/stop repl-server)))


(defn stop-app []
  (doseq [component (:stopped (mount/stop))]
    (timbre/info component "stopped"))
  (shutdown-agents))

(defn start-app [args]
  (doseq [component (-> args
                        (parse-opts cli-options)
                        mount/start-with-args
                        :started)]
    (timbre/info component "started"))
  (.addShutdownHook (Runtime/getRuntime) (Thread. stop-app)))

(def default-timestamp-opts
  "Controls (:timestamp_ data)"
  {:pattern  :iso8601     #_"yy-MM-dd HH:mm:ss"
   :locale   :jvm-default #_(java.util.Locale. "en")
   :timezone (java.util.TimeZone/getDefault)         #_(java.util.TimeZone/getTimeZone "Europe/Amsterdam")
   })

(defn logging-params [params]
  (assoc-in params [:appenders :spit]
                (appenders/spit-appender {:fname (:fname params)})))

(defn -main [& args]
  ;; for http access cdn node
  (System/setProperty "sun.net.http.allowRestrictedHeaders", "true")

  (mount/start #'rainmote.config/env)
  (-> (:logging env)
      logging-params
      (assoc ,,, :timestamp-opts default-timestamp-opts)
      timbre/merge-config!)

  (let [{:keys [options]} (parse-opts args cli-options)]
    (println options)
    (cond
      (nil? (:database-url env))
      (do
        (timbre/error "Database configuration not found, :database-url environment variable must be set before running")
        (System/exit 1))

      (some #{"init"} args)
      (do
        (migrations/init (select-keys env [:database-url :init-script]))
        (System/exit 0))

      (migrations/migration? args)
      (do
        (timbre/info args)
        #_(migrations/migrate args (select-keys env [:database-url]))
        (if (some #{"create"} args)
          (migrations/create (last args) (select-keys env [:database-url]))
          (migrations/migrate (rest args) (select-keys env [:database-url])))
        (System/exit 0))

      (some-> options :ruokuai)
      (do
        (ruokuai/parse-12306 (-> options :ruokuai))
        (System/exit 0))
    :else
    (start-app args))))
