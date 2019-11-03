(ns rainmote.ticket-tool.add.ui
  (:require [rainmote.common.antd :as ant]))

(defn page []
  [:div
   [ant/Steps {:type :navigation
               :current 0
               :style {:marginBottom 60
                       :boxShadow "0px -1px 0 0 #e8e8e8 inset"}
               }
    [ant/Step {:status "process" :title "选择账号"}]
    [ant/Step {:status "wait" :title "填写抢票信息"}]
    [ant/Step {:status "wait" :title "提交"}]
    ]])
