(ns rainmote.tools.ip
  (:require [rainmote.common.user-agent :as ua]
            [rainmote.common.http :as http]))

(def url "http://ip-api.com/batch?lang=zh-CN")

(def default-headers
  {"Accept-Encoding" "gzip, deflate"})

(defn construct-headers [header]
  (-> default-headers
      (assoc , "User-Agent" (ua/get-user-agent))
      (merge , (or header {}))))

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

(defn query [iplist]
  (->> iplist
       (partition-all 100 ,)
       (map #(batch-query {:iplist %}) ,)
       (flatten ,)))

(comment
 (batch-query {:iplist ["114.114.114.114" "112.10.106.99"]})
 )