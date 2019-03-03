(ns rainmote.intelligence.weibo
  (:require [rainmote.common.http :as http]
            [rainmote.common.user-agent :as ua]
            [net.cgrand.enlive-html :as html]))

(defn get-html-data [url]
  (-> {:method :get
       :url url
       :headers {:User-Agent (ua/get-user-agent)}}
      http/request
      :body
      html/html-snippet
      (html/select ,,, [:div#pl_top_realtimehot :table :tbody :tr])))

(defn get-seq [c]
  (cond
    (and (nil? (-> c :content first :content))
         (= "icon-top" (-> c :content first :attrs :class)))
    0

    (= "td-01 ranktop" (-> c :attrs :class))
    (-> c :content first Integer/parseInt)

    :else (println "Unkown seq" (type c) c)))

(defn get-label [c]
  (cond
    (empty? c) ""
    (or (list? c) (seq? c)) (-> c first (html/select ,,, [:i]) first html/text)
    :else (println "Unkown label" (type c))))

(defn get-data [n]
  (->> "https://s.weibo.com/top/summary?cate=realtimehot"
       get-html-data
       (map #(-> {}
                 (assoc ,,, :seq (-> (html/select % [:td.td-01]) first get-seq))
                 (assoc ,,, :topic (-> (html/select % [:td.td-02 :a]) first html/text))
                 (assoc ,,, :href (str "https://s.weibo.com"
                                       (-> (html/select % [:td.td-02 :a]) first :attrs :href)))
                 (assoc ,,, :label (-> (html/select % [:td.td-03]) get-label)))
            ,,,)
       (take n ,,,)))

(comment
  (get-data 10)
  )
