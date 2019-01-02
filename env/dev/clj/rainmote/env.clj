(ns rainmote.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [rainmote.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[rainmote started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[rainmote has shut down successfully]=-"))
   :middleware wrap-dev})
