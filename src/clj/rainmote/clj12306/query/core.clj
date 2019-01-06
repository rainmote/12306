(ns rainmote.clj12306.query.core
  (:require [clojure.core.async :as a]
            [clj-http.cookies]
            [taoensso.timbre :as timbre]
            [rainmote.common.http :as http]
            [clj-http.client :as client]
            [rainmote.common.user-agent :as ua]))

(def ^:dynamic *cdn-nodes* (atom []))
(def ^:dynamic *cdn-node-meta* (atom {}))
;;;; struct:
;;;;    {"1.1.1.1" {:cookie obj
;;;;                :user-agent "xxx"
;;;;                :query-url "leftTicket/queryZ"}}

(def domain "kyfw.12306.cn")

(def default-params
  {:headers {"Host" domain
             "Accept-Language" "zh-CN,zh;q=0.9"
             "Accept" "*/*"}
   :proxy-host "127.0.0.1"
   :proxy-port 8080
   :insecure? true
   :ignore-unknown-host? true
   :throw-exceptions false
   :conn-timeout 2000
   :socket-timeout 2000})

(def left-ticket-url "https://%s/otn/leftTicket/init")

(defn find-query-url [body]
  ;; 查询余票url会变化，应该从结果中动态获取
  (re-find #"leftTicket/query\w" body))

(defn get-meta
  [{:keys [node timeout]}]
  (let [meta
        (get @*cdn-node-meta* node)

        find-query-url
        (fn [body]
          (re-find #"leftTicket/query\w" body))

        process-result
        (fn [resp]
          (if (= 200 (-> resp :status))
            (find-query-url (-> resp :body))
            ;; 从cdn-nodes缓存中删除该节点
            (swap! *cdn-nodes* #(remove #{node} %))))]
    (if meta
      ;; 如果缓存中找到该节点meta，直接返回
      (do
        meta)
      ;; 否则获取相关信息
      (do
        (timbre/infof "node:%s not found meta, will create it" node)
        (let [cs (clj-http.cookies/cookie-store)
              user-agent (ua/get-user-agent)

              ;; 从resp html中解析出查询url(该url会变化)
              query-url
              (try
                (-> {:method :get
                     :url (format left-ticket-url node)}
                    (merge ,,, default-params)
                    (assoc ,,, :cookie-store cs)
                    (assoc ,,, :conn-timeout timeout)
                    (assoc ,,, :socket-timeout timeout)
                    (assoc-in ,,, [:headers "User-Agent"] user-agent)
                    client/request
                    process-result)
                (catch Exception e
                  (swap! *cdn-nodes* #(remove #{node} %))
                  (timbre/warnf "init cdn cookie failed when get-meta, remove node from cache, node:%s, exception:%s" node (str e))))

              ;; 组合相关meta
              new-meta {:cookie cs :query-url query-url :user-agent user-agent}]
          (when (some? query-url)
            ;; 更新meta到缓存
            (swap! *cdn-node-meta* assoc node new-meta)
            new-meta))))))

(defn update-cdn-node [domain]
  (reset! *cdn-nodes* ["113.104.14.227" "58.51.168.47" "112.240.60.88"])
  (->> @*cdn-nodes*
       (pmap #(get-meta {:node %
                         :timeout 5000})
             ,,,)
       doall)
  (timbre/infof "update cdn nodes, count:%s" (count @*cdn-nodes*)))

(defn update-cdn-thread []
  (loop []
    (when (< (count @*cdn-nodes*) 1)
      (timbre/info "[CDN线程] CDN节点不足，开始重新获取CDN节点")
      (update-cdn-node domain))
    (timbre/debug "[CDN线程] Sleep...")
    (Thread/sleep 10000)
    (recur)))

(def train-info
  [:secretStr
   :buttonTextInfo
   :train_no
   :station_train_code
   :start_station_telecode
   :end_station_telecode
   :from_station_telecode
   :to_station_telecode
   :start_time
   :arrive_time
   :lishi
   :canWebBuy
   :yp_info
   :start_train_date
   :train_seat_feature
   :location_code
   :from_station_no
   :to_station_no
   :is_support_card
   :controlled_train_flag
   :gg_num
   :gr_num ;;高级软卧
   :qt_num ;;其他
   :rw_num ;;软卧
   :rz_num ;;软座
   :tz_num ;;特等座
   :wz_num ;;无座
   :yb_num
   :yw_num ;;硬卧
   :yz_num ;;硬座
   :ze_num ;;二等座
   :zy_num ;;一等座
   :swz_num ;;商务座
   :srrb_num ;;动卧
   :yp_ex
   :seat_types
   :exchange_train_flag
   :houbu_train_flag])

(defn parse-single-train [s]
  (->> (clojure.string/split s #"\|")
       (interleave train-info ,,,)
       (apply assoc {} ,,,)))

(defn parse-result [resp node]
  (if (and (= 200 (-> resp :httpstatus))
           (some? (-> resp :status)))
    (->> (-> resp :data :result)
         (map parse-single-train ,,,))
    ;; 如果失败，则将cdn node meta从缓存中移除
    (swap! *cdn-node-meta* dissoc node)
    ))

(defn convert-seat-name->key [name]
  (-> {"商务座" :swz_num
       "一等座" :zy_num
       "二等座" :ze_num
       "软卧" :rw_num
       "硬卧" :yw_num
       "硬座" :yz_num
       "无座" :wz_num}
      (get ,,, name)
      (or ,,,
       ;; 未获取到key
       (do
         (timbre/error "座位转换为key错误! 使用默认值:硬卧")
         :yw_num))))

(defn can-buy-seat-ticket? [info seat-name tickets]
  (let [k (convert-seat-name->key seat-name)
        v (-> info k)]
    (if (and (not= v "无")
             (not= v "")
             (or (= v "有")
                 (some-> v Integer/parseInt (>= ,,, tickets))))
      true
      false)))

(defn process-train-infos
  [infos {:keys [node trains seats tickets]}]
  (->> infos
       ;; 过滤指定且可购买的车次
       (filter (fn [x]
                 (and (some #(= % (-> x :station_train_code)) trains)
                      (= "Y" (-> x :canWebBuy))))
               ,,,)
       ;; 将可购座位信息聚合
       (map (fn [x]
              (-> {:info x}
                  ;; 返回可以买的座位信息, 比如：["硬座" "硬卧"]
                  (assoc ,,, :seats
                             (filter #(can-buy-seat-ticket? x % tickets) seats))))
            ,,,)
       ;; 过滤有可买座位的车次
       (filter #(-> % :seats not-empty) ,,,)
       (assoc {:node node} :trains ,,,)
       ))

(defn query
  [{:keys [node date from to] :as params}]
  (let [ch (a/chan)]
    ;; 启动一个线程来访问单个CDN
    (a/thread
     (let [;; 将结果放在channel中
           put-ch-fn (fn [result ch]
                       (when-not (empty? result)
                         (a/>!! ch result)))
           ;; 获取内存cache中的meta
           meta      (get-meta {:node node})]
       (if meta
        (try
          (some-> {:method       :get
                    :as           :json
                    :url          (format "https://%s/otn/%s" node (:query-url meta))
                    :query-params {"leftTicketDTO.train_date"   date
                                  "leftTicketDTO.from_station" from
                                  "leftTicketDTO.to_station"   to
                                  "purpose_codes"              "ADULT"}}
                  (merge ,,, default-params)
                  (assoc ,,, :cookie-store (:cookie meta))
                  (assoc-in ,,, [:headers "Referer"] "https://kyfw.12306.cn/otn/leftTicket/init")
                  (assoc-in ,,, [:headers "User-Agent"] (:user-agent meta))
                  client/request
                  :body
                  ;; 检查结果,并将单个车次解析为字典结构
                  (parse-result ,,, node)
                  ;; 按照请求条件过滤出可购票的车次
                  (process-train-infos ,,, params)
                  ;; 结果保存至channel
                  (put-ch-fn ,,, ch))
          (catch Exception e
            (timbre/warnf "query ticket info failed, node: %s" node)))
        ;; 获取meta为空，初始化meta请求失败
        (do
          (timbre/warnf "Init meta failed, will cancel request, and remove cdn node: %s" node)
          (swap! *cdn-nodes* #(remove #{node} %))
          ))))
    ;; 主线程直接返回channel
    ch))

(defn start-update-cdn-thread []
  (a/thread-call update-cdn-thread))

(defn query-left-ticket-info
  [{:keys [timeout cdn-num]
    :or {timeout 1000
         cdn-num Integer/MAX_VALUE}
    :as params}]
  (start-update-cdn-thread)
  (if (< (count @*cdn-nodes*) 1)
    (timbre/info "Not found cdn-nodes, please call start-update-cdn-thread for start update thread")
    (do
      (timbre/infof "当前cdn节点数量:%s" (count @*cdn-nodes*))
      (->> @*cdn-nodes*
          shuffle
          ;; 随机挑选指定数量的cdn节点
          (take cdn-num ,,,)
          (mapv #(-> {:node %}
                      (merge ,,, params)
                      query)
                ,,,)
          ;; 得到所有channel, 并加入超时时间
          (#(conj % (a/timeout timeout)) ,,,)
          ;; 等待channel返回
          a/alts!!
          ;; 处理channel信息
          ((fn [[v ch]]
              (if v
                (-> v :node)
                ;; 当v为空时，说明channel超时了
                (timbre/errorf "no resp, channel timeout[%s]!" timeout)))
            ,,,)))))

(comment
  (query-left-ticket-info {:date "2019-01-18"
                           :from "HZH"
                           :to "XAY"
                           :trains ["1154" "Z86" "G1874"]
                           :seats ["硬座" "商务座"]
                           :tickets 3
                           :cdn-num 5})
)
