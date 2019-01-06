(ns rainmote.tools.ruokuai
  (:require [rainmote.common.http :as http]
            [rainmote.config :refer [env]]
            [taoensso.timbre :as timbre]
            [digest]
            [clojure.java.io :as io])
  (:import [java.util.Base64]))

(defn get-default-params []
  {:url "http://api.ruokuai.com/create.json"
   :method :post
   :headers {"Connection" "Keep-Alive"
             "Expect" "100-continue"
             "Content-Type" "application/json"}
   :form-params {:username (-> env :ruokuai :username)
                 :password (digest/md5 (-> env :ruokuai :password))
                 :softid (-> env :ruokuai :softid)
                 :softkey (-> env :ruokuai :softkey)
                 :timeout 100}
   :socket-timeout 110000
   :conn-timeout 110000})

(defn file->bytes [file]
  (with-open [in (io/input-stream (io/file file))
              out (java.io.ByteArrayOutputStream.)]
    (io/copy in out)
    (.toByteArray out)))

(defn base64-encode [x]
  (.encodeToString (java.util.Base64/getEncoder) x))

(defn process-resp [resp]
  (let [result (some-> resp :body "Result")]
    (-> (= (:status resp) 200)
        (and ,,, (some? result))
        (if ,,,
          result
          (timbre/error "Access ruokuai failed:" (some-> resp :body))))))

(defn parse
  [file typeid]
  (timbre/info "Request ruokuai:" (timbre/get-env))
  (-> (get-default-params)
      (assoc-in ,,, [:form-params :image]
                    (-> file file->bytes base64-encode))
      (assoc-in ,,, [:form-params :typeid] typeid)
      http/request
      process-resp))

(defn parse-12306
  [file]
  (parse file 6137))
