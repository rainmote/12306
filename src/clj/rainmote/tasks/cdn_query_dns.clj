(ns rainmote.tasks.cdn-query-dns
  (:require [clojure.core.async :refer [<! go-loop >!! <!! chan timeout close!]]
            [taoensso.timbre :as log]
            [rainmote.common.dns :as dns]
            [rainmote.db.helper :refer [add-ip-to-server]]
            [mount.core :refer [defstate]]
            [rainmote.config :refer [env]]
            ))

(def ch (chan 10))

(defn start-bg-read-dns-server-file
  [{:keys [path batch-size]
    :or {batch-size 100}}]
  (go-loop []
    (with-open [rdr (clojure.java.io/reader path)]
      (->> rdr
           line-seq
           shuffle
           (partition-all batch-size ,,,)
           (map #(>!! ch %) ,,,)
           doall))
    (log/info "dns file process finished, will sleep 60s")
    (<! (timeout 60000))
    (recur)))

(defn start-bg-dns-query
  [{:keys [domain tag]
    :or {domain "kyfw.12306.cn"
         tag "dns"}
    :as params}]
    (go-loop []
           (when-let [server-list (<!! ch)]
             (log/debug "process server list, count: " (count server-list))
             (log/debug "first server: " (first server-list))
             (try
               (->> server-list
                    (map #(dns/lookup-answers {:name   domain
                                               :server %}))
                    flatten
                    (filter identity)
                    set
                    (#(do
                        (log/debug "dns query ip count: " (count %))
                        (log/debug (take 10 %))
                        (identity %)))
                    (apply vector)
                    (assoc params :ips ,,,)
                    add-ip-to-server
                    count
                    (#(when (pos? %) (log/info "found new ip count: " %))))
               (catch Exception e
                 (.printStackTrace e)
                 (println "request failed" (str e))))
             (log/debug "sleep 1s")
             (<! (timeout 1000))
             (recur))))

(defstate task
          :start (do
                   (println "start task dns-query")
                   (log/info "starting background task [dns-query]")
                   (start-bg-dns-query {})
                   (start-bg-read-dns-server-file {:path (-> env :dns-query :dns-server-file)})
                   (log/info "background task [dns-query] started!")
                   true)
          :stop (do
                  (close! ch)
                  (log/info "background task [dns-query] stopped!")))