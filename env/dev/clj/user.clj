(ns user
  (:require [rainmote.config :refer [env]]
            [clojure.spec.alpha :as s]
            [expound.alpha :as expound]
            [mount.core :as mount]
            [rainmote.figwheel :refer [start-fw stop-fw cljs]]
            [rainmote.core :refer [start-app]]
            [rainmote.db.core]
            [conman.core :as conman]
            [luminus-migrations.core :as migrations]))

(alter-var-root #'s/*explain-out* (constantly expound/printer))

(defn start []
  (mount/start-without #'rainmote.core/repl-server))

(defn stop []
  (mount/stop-except #'rainmote.core/repl-server))

(defn restart []
  (stop)
  (start))

(defn restart-db []
  (mount/stop #'rainmote.db.core/*db*)
  (mount/start #'rainmote.db.core/*db*)
  (binding [*ns* 'rainmote.db.core]
    (conman/bind-connection rainmote.db.core/*db* "sql/queries.sql")))

(defn reset-db []
  (migrations/migrate ["reset"] (select-keys env [:database-url])))

(defn migrate []
  (migrations/migrate ["migrate"] (select-keys env [:database-url])))

(defn rollback []
  (migrations/migrate ["rollback"] (select-keys env [:database-url])))

(defn create-migration [name]
  (migrations/create name (select-keys env [:database-url])))


