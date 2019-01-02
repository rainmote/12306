(ns rainmote.app
  (:require [rainmote.core :as core]))

;;ignore println statements in prod
(set! *print-fn* (fn [& _]))

(core/init!)
