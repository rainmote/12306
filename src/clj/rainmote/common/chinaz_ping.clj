(ns rainmote.common.chinaz-ping
  (:require [cheshire.core :as json]
            [cheshire.factory :as fact]
            [clj-http.cookies]
            [clj-http.client :as client]
            [taoensso.timbre :as log]
            [net.cgrand [enlive-html :as html]]
            [rainmote.common.user-agent :as ua]))

(def url-prefix "http://ping.chinaz.com/")
;;(def url-prefix "http://tool.chinaz.com/speedtest/")

(defonce ^:dynamic *cookie* (atom (clj-http.cookies/cookie-store)))

(def http-proxy
  {:proxy-host "127.0.0.1"
   :proxy-port 8080})

(def default-headers
  {"Origin" url-prefix
   "Connection" "keep-alive"
   "Content-Type" "application/x-www-form-urlencoded"
   "Accept" "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"
   "Referer" url-prefix
   "Accept-Encoding" "gzip, deflate"
   "Accept-Language" "zh-CN,zh;q=0.9"})

(defn construct-headers [header]
  (-> default-headers
      (assoc ,,, "User-Agent" (ua/get-user-agent))
      (merge ,,, (or header {}))))

(defn retry-handler [ex times ctx]
  (if (> times 1) false true))

(defn construct-params
  [{:keys [form-params headers]
    :or {form-params {}
         headers {}}
    :as param}]
  (-> {:cookie-store @*cookie*}
      (merge , param)
      ;;(merge , http-proxy)
      (assoc , :throw-exceptions false)
      ;;(assoc , :retry-handler retry-handler)
      (assoc , :ignore-unknown-host? true)
      (assoc , :form-params form-params)
      (assoc , :headers (construct-headers headers))))

(defn get-html-data
  [{:keys [method url params]
    :or {method client/get}}]
  (try
     (-> (method url params)
         :body
         html/html-snippet)
     (catch Exception e
       (log/error "get html data error," (str e)))))

(defn get-home-body [domain]
  (-> {}
      (assoc , :method client/get)
      (assoc , :url (str url-prefix domain))
      (assoc , :params (construct-params {:form-params {:linetype "电信,多线,联通,移动,海外"}
                                          :socket-timeout 120000
                                          :conn-timeout 120000}))
      (get-html-data ,)))

(defn get-enkey [body]
  (-> body
      (html/select , [:input#enkey])
      (:content ,)))

(defn get-all-guid [body]
  (->> (html/select body [:div.row.listw.tc.clearfix])
       (map #(some-> % :attrs :id) ,)))

(defn get-request-meta [domain]
  (let [body (get-home-body domain)
        get-v #(-> (html/select body %) first :attrs :value)]
    (-> {}
        (assoc , :encode (get-v [:input#enkey]))
        (assoc , :ishost (get-v [:input#ishost]))
        (assoc , :checktype (get-v [:input#checktype]))
        ;(assoc , :ftype (get-v [:input#ftype]))
        (assoc , :guids (->> (html/select body [:div.row.listw.tc.clearfix])
                             (map #(some-> % :attrs :id) ,))))))

(defn parse-jsonp [d]
  (binding [fact/*json-factory* (-> {:allow-single-quotes true
                                     :allow-unquoted-field-names true}
                                    fact/make-json-factory)]
    (try
      (json/parse-string d true)
       (catch Exception e
         (log/info "parse json failed, content: " d)
         #_(throw e)))))


(defn request-single-proxy
  [{:keys [domain guid meta]}]
  (some->>
    {:query-params {:t "ping"}
     :form-params (-> {:host domain
                       :guid guid}
                      (merge , meta))
     :headers {"Referer" (str url-prefix domain)
               "Accept" "text/javascript, application/javascript, */*; q=0.01"}
     :socket-timeout 120000
     :conn-timeout 120000}
    (construct-params ,)
    (client/post (str url-prefix "iframe.ashx") ,)
    :body
    ;; Remove the front and rear brackets
    ((comp (partial apply str) drop-last rest) ,)
    (parse-jsonp ,)
    :result
    :ip))

(defn get-cdn-node
  "return cdn node set"
  [domain]
  (let [meta (get-request-meta domain)
        f (fn [guid]
            (try
              (-> {:domain domain
                   :meta   (dissoc meta :guids)}
                  (assoc , :guid guid)
                  (request-single-proxy ,))
              (catch Exception e
                (log/error "get-cdn-node failed" (str e))
                (log/error (.printStackTrace e)))))]
    (log/info "Proxy node count: " (count (:guids meta)))
    (->> (:guids meta)
         (pmap f ,)
         ;; filter nil
         (filter identity ,)
         (into #{} ,))))

(comment
  (get-cdn-node "kyfw.12306.cn")
  )