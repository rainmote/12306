(ns rainmote.common.dns
  (:import (org.xbill.DNS
            Type
            DClass
            Lookup
            SimpleResolver))
  )

(defn lookup
  [{:keys [name type dclass server]
    :or {type Type/A
         dclass DClass/IN}}]
  (let [sr (if server
             (SimpleResolver. server)
             (SimpleResolver.))
        l (Lookup. name type dclass)]
    (.setResolver l sr)
    (.run l)
    {:aliases (seq (.getAliases l))
     :answers (if (= (.getResult l) Lookup/SUCCESSFUL)
                (seq (.getAnswers l))
                [])}))

(defn lookup-answers
  [{:keys [name type dclass server throw-exception?]
    :or {type Type/A
         dclass DClass/IN
         throw-exception? false}
    :as params}]
  (try
    (->> (lookup params)
         :answers
         (map #(-> % .getAddress .getHostAddress) ,,,))
    (catch Exception e
      (when throw-exception?
        (throw e))))
  )

(comment
  (lookup {:name "kyfw.12306.cn"})
  (->> (lookup-answers {:name "kyfw.12306.cn"
                        :server "114.114.114.114"}))
  )
