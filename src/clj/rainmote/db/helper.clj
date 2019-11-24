(ns rainmote.db.helper
  (:require [rainmote.db.core :as db]
            [taoensso.timbre :as log]
            [mount.core :refer [defstate]]
            [rainmote.common.ip :as ip]))

(defonce ^:dynamic *ips-cache* (atom #{}))

(defonce server-spec
         {:regionName ""
          :timezone ""
          :as ""
          :city ""
          :isp ""
          :region ""
          :org ""
          :status ""
          :zip ""
          :lon 0
          :lat 0
          :tag ""
          :domain ""
          :query ""
          :country ""
          :countryCode ""})

(defn add-ip-to-server
  [{:keys [domain tag ips]}]
  (when (empty? @*ips-cache*)
    (reset! *ips-cache* (->> (db/query-all-ip-from-server)
                             (map #(-> % :ip))
                             (into #{}))))
  (let [new-ips (clojure.set/difference (into #{} ips) @*ips-cache*)]
    (when-not (empty? new-ips)
      ;; 更新cache
      ;;(swap! *ips-cache* (partial apply conj) new-ips)

      (log/info "add ip to server, count: " (count new-ips))

      (->> (apply vector new-ips)
           ;; 查询ip信息
           ;;(#(do (println %) (identity %)))
           ip/query
           ;; 上传数据库
           (map #(-> (merge server-spec %)
                     (merge , {:domain domain
                               :tag tag})
                     (db/insert-server-table)
                     (= , 1)
                     (when
                       (swap! *ips-cache* into (-> % :query)))))
           doall
           (filter some?)))))

(comment
  (->> (db/query-all-ip-from-server)
       (map #(-> % :ip))
       set
       )

  (->> "/Users/one/my_project/rainmote/12306_ips"
       slurp
       clojure.string/split-lines
       (map #(clojure.string/trim %))
       (assoc {:tag "cdn"
               :domain "kyfw.12306.cn"} :ips ,,,)
       add-ip-to-server
       )
  )
