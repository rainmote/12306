(ns rainmote.query-dns
  (:require [clojure.core.async :refer [<! go-loop >!! <!! chan timeout close!]]
            [taoensso.timbre :as timbre]
            [clojure.tools.logging :as log]
            [rainmote.common.dns :as dns]
            [rainmote.common.ip :as ip]
            [rainmote.db.core :as db]
            [mount.core :as mount]
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
           (partition-all batch-size ,,,)
           (map #(>!! ch %) ,,,)
           doall))
    (log/info "dns file process finished, will sleep 60s")
    (<! (timeout 60000))
    (recur)))

(defn start-bg-dns-query
  [{:keys [domain tag]
    :or {domain "kyfw.12306.cn"
         tag "cdn"}}]
  (let [cache (atom #{})]
    (reset! cache (->> (db/query-all-ip-from-server)
                       (map #(-> % :ip) ,,,)
                       (into #{})))
    (go-loop []
           (when-let [server-list (<!! ch)]
             (println  "process server count" (count server-list))
             (log/info "process server list, count: " (count server-list))
             (try
               (->> server-list
                    (map #(dns/lookup-answers {:name   domain
                                               :server %}))
                    flatten
                    ;; 把已知ip剔除掉
                    (filter #(if (or (contains? @cache %)
                                     (nil? %))
                               false
                               true) ,,,)
                    ;; 更新cache
                    (#(do
                        (swap! cache (partial apply conj) %)
                        (identity %)))
                    (#(do
                        (log/info "found new ip, count: " (count %))
                        (identity %)))
                    ip/query
                    (map #(-> (merge % {:domain domain
                                        :tag tag})
                              db/insert-server-table))
                    doall)
               (catch Exception e
                 (.printStackTrace e)
                 (println "request failed" (str e))))
             (log/info "sleep 5s")
             (<! (timeout 5000))
             (recur)))))

(mount/defstate task
          :start (do
                   (println "start task dns-query")
                   (log/info "starting background task [dns-query]")
                   (start-bg-dns-query {})
                   (start-bg-read-dns-server-file {:path (-> env :dns-query :dns-server-file)})
                   (log/info "background task [dns-query] started!")
                   true)
          :stop (do
                  (println task)
                  (close! ch)
                  (log/info "background task [dns-query] stopped!")))