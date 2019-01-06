(ns rainmote.tools.ifttt
  (:require [rainmote.common.http :as http]
            [taoensso.timbre :as timbre]))

(defn get-url [event]
  (format "https://maker.ifttt.com/trigger/%s/with/key/%s" (-> env :ifttt :key)))

(defn send [event &msg]
  "(send event value1 value2 value3)"
  (->> {:method :post
        :url (get-url event)
        :form-params (->> msg
                          (interleave ["value1" "value2" "value3"] ,,)
                          (apply assoc {}))}
      (timbre/spy :info ,,,)
      http/request))
