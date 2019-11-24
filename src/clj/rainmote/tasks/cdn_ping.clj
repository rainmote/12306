(ns rainmote.tasks.cdn-ping
  (:require [rainmote.common.chinaz-ping :refer [get-cdn-node]]
            [rainmote.db.helper :refer [add-ip-to-server]]
            [mount.core :refer [defstate]]
            [taoensso.timbre :as log]
            [clojure.core.async :as a]))


(defn start-bg-ping
  [{:keys [domain tag]
    :or {domain "kyfw.12306.cn"
         tag "chinaz-ping"}
    :as params}]
  (a/go-loop []
    (->> (get-cdn-node domain)
         (assoc params :ips ,,,)
         add-ip-to-server
         count
         (#(when (pos? %) (log/info "found new ip count: " %))))
    (a/<! (a/timeout 180000))
    (recur)))

(defstate bg-chinaz-ping
          :start (do
                   (start-bg-ping {})))

