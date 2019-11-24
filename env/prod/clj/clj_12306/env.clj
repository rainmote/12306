(ns rainmote.env
  (:require [taoensso.timbre :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[rainmote started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[rainmote has shut down successfully]=-"))
   :middleware identity})
