(ns rainmote.common.ip
  (:require [rainmote.common.user-agent :as ua]
            [rainmote.common.http :as http]
            [mount.core :refer [defstate]]
            [clojure.core.async :as a]))

(def url "http://ip-api.com/batch?lang=zh-CN")

(def default-headers
  {"Accept-Encoding" "gzip, deflate"})

(defn construct-headers [header]
  (-> default-headers
      (assoc , "User-Agent" (ua/get-user-agent))
      (merge , (or header {}))))

(defn retry-handler [ex times ctx]
  (if (> times 1) false true))

(defn construct-params
  [{:keys [form-params headers]
    :or {form-params {}
         headers {}}
    :as params}]
  (-> (merge , params)
      (assoc , :content-type :json)
      (assoc , :accept :json)
      (assoc , :as :json)
      (assoc , :throw-exceptions false)
      (assoc , :retry-handler retry-handler)
      (assoc , :ignore-unknown-host? true)
      (assoc , :conn-timeout 10000)
      (assoc , :socket-timeout 10000)
      (assoc , :form-params form-params)
      (assoc , :headers (construct-headers headers))))

(defn batch-query
  [{:keys [iplist fields]}]
  (->> iplist
       (map #(assoc {} :query %) ,)
       ;; 如果有指定字段则请求时携带
       (map #(if fields
               (assoc % :fields fields)
               (identity %)) ,)
       (assoc {:method :post
               :url url} :form-params ,)
       (construct-params ,)
       http/request
       :body))

(defonce ^:private ch (a/chan 1000))

(defn start []
  (a/go-loop []
           (when-let [[ips out-ch] (a/<!! ch)]
             ;;(println "get task ips:" ips)
             (a/>!! out-ch (batch-query {:iplist ips}))
             (a/<! (a/timeout 1000)) ;; 流控
             (recur))))

(defstate bg-ip-query
          :start (start)
          :stop (a/close! ch))

(defn query [iplist]
  (let [out-ch (a/chan 100)]
    (->> (partition-all 100 iplist)
         (map #(a/>!! ch (vector % out-ch)))
         (map (fn [_] (a/<!! out-ch)))
         doall
         flatten)))

(comment
 (batch-query {:iplist ["114.114.114.114" "112.10.106.99"]})
 (query ["114.114.114.114" "1.1.1.1"])
 )