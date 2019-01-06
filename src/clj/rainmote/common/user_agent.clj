(ns rainmote.common.user-agent
  (:require [cheshire.core :as json]
            [clojure.java.io :as io]))

;;;; https://github.com/hellysmile/fake-useragent
;;;; 使用python库生成user agent，然后保存为json格式
;;;; 本文件主要提供接口获取已经生成好的user agent
;;;; command:
;;;;    cd resources/user_agent && python gen.py
;;;; result file:
;;;;    resources/user_agent/user_agent.json

(def filepath "user_agent/user_agent.json")

(defn get-user-agent []
  (-> filepath
      io/resource
      slurp
      json/parse-string
      rand-nth))