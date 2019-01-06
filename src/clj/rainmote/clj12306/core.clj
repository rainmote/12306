(ns rainmote.clj12306.core
  (:require [clj-http.client :as client]))

(defn input-pipeline
  [ctx]
  ctx)

(defn run
  [{:keys [flow-map flow option]}]
  (loop [ctx option
         steps (-> flow-map :flow flow)]
    (if (empty? steps)
      ctx
      (-> (flow-map :global)

          (merge , (-> flow-map :steps (first steps)))
          (merge , ctx)

          (input-pipeline ,)
          (input-checker ,)
          (process ,)
          (output-checker ,)
          (output-pipeline ,)

          (recur , (rest setps))
          ))))

(comment
 (run {:flow-map flow-12306
       :flow :query-contacts
       :option {:username "mapletianwei"
                :password "tianwei2014"}}))

(def flow-12306
  {:global {:protocol :https
            :domain "kyfw.12306.cn"
            :params {}}

   :flow {:query-contacts [:init :login-captcha]}

   :steps {:init
           {:desc "获取Cookie"
            :url "/otn/resources/login.html"
            :method :get
            :success [:login-captcha]
            :failure [:init]}

           :login-captcha
           {:desc "获取登陆验证码"
            :url "/passport/captcha/captcha-image64"
            :method :get
            :params {:query-params {:login_site "E"
                                    :module "login"
                                    :rand (str "sjrand&" (rand))}}
            :output-pipeline [#'get-image #'analyze-image]
            :success [:check-capatcha]
            :failure [:init]}

           :check-captcha
           {:desc "校验验证码"
            :url "/passport/captcha/captcha-check"
            :method :get
            :params {:query-params {:rand "sjrand"
                                    :login_site "E"
                                    :_ (System/currentTimeMillis)}}
            :checker {:input [:login-answer]
                      :result #(-> % :body parse-jsonp :result_code (= , "4"))}
            :success [:login]
            :failure [:login-captcha]}

           :login
           {:desc "登陆"
            :url "/passport/web/login"
            :method :post
            :params {:form-params {:appid "otn"}}
            :output-pipeline [#(-> % :body :uamtk)]
            :checker {:input [:username :password :login-answer]
                      :output [:uamtk]
                      :result #(-> % :body :result_code (= , 0))}
            :success [:x-user-login]
            :failure [:init]}

           :x-user-login
           {:desc ""
            :url "/otn/login/userLogin"
            :method :get
            :success [:x-passport]
            :failure [:init]}

           :x-passport
           {:desc ""
            :url "/otn/passport?redirect=/otn/login/userLogin"
            :method :get
            :success [:auth-token]
            :failure [:init]}

           :auth-token
           {:desc "校验Token"
            :url "/passport/web/auth/uamtk"
            :method :post
            :params {:form-params {:appid "otn"}}
            :output-pipeline [#(-> % :body :newapptk)]
            :checker {:result #(-> % :body :result_code (= , 0))
                      :output [:newapptk]}
            :success [:uam-auth-token]
            :failure [:init]}

           :uam-auth-token
           {:desc "校验UAM Token"
            :url "/otn/uamauthclient"
            :method :post
            :checker {:result #(-> % :body :result_code (= , 0))}
            :success [:get-conf]
            :failure [:init]}

           :get-conf
           {:desc "获取Session信息"
            :url "/otn/login/conf"
            :method :post
            :checker {:result #(-> % :body :status (= , true))}
            :success [:init-api]
            :failure [:init]}

           :init-api
           {:desc "初始化Session API"
            :url "/otn/index/initMy12306Api"
            :method :post
            :checker {:result #(-> % :body :status (= , true))}
            :success [:query-contacts]
            :failure [:init]}

           :query-contacts
           {:desc "获取联系人信息"
            :url "/otn/passengers/query"
            :method :post
            :params {:form-params {:pageIndex 1 :pageSize 30}}
            :output-pipeline [#'process-contacts]
            :checker {:result #(-> % :body :status (= , true))}
            :success [:all]
            :failure [:init]}
           }})