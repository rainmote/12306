(ns rainmote.routes.home
  (:require [rainmote.layout :as layout]
            [rainmote.db.core :as db]
            [rainmote.websocket :as ws]
            [compojure.core :refer [defroutes GET POST]]
            [ring.util.http-response :as response]
            [clojure.java.io :as io]))

(defn home-page []
  (layout/render "index.html"))


(defroutes home-routes
  (GET "/" []
       (home-page))
           (GET  "/chsk" req (ws/ring-ajax-get-or-ws-handshake req))
           (POST "/chsk" req (ws/ring-ajax-post req))
  (GET "/graphiql" [] (layout/render "graphiql.html"))
  (GET "/docs" []
       (-> (response/ok (-> "docs/docs.md" io/resource slurp))
           (response/header "Content-Type" "text/plain; charset=utf-8"))))

