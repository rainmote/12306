(ns rainmote.common.http
  (:require [clj-http.client :as client]
            [taoensso.timbre :as timbre]))

(def default-headers
  {"User-Agent" "Mozilla/5.0 (Macintosh; Intel Mac OS X 10.6; rv:2.0.1) Gecko/20100101 Firefox/4.0.1"})

(defn retry-handler [ex times ctx]
  (timbre/warn ctx ex)
  (if (> times 2) false true))

(def default-params
  {:throw-exceptions false
   :retry-handler retry-handler
   })

(defn construct-headers [header]
  (-> default-headers
      (merge ,,, (or header {}))))

(defn construct-params
  [params]
  (-> default-params
      (merge ,,, params)
      (assoc ,,, :headers (construct-headers (:headers params)))))

(defn request [params]
  (->> (construct-params params)
       (timbre/spy :debug ,,,)
       client/request))
