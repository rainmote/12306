(ns rainmote.websocket
  (:require [taoensso.timbre :as log]
            [mount.core :refer [defstate]]
            [taoensso.sente :as sente]
            [taoensso.sente.server-adapters.http-kit :refer (get-sch-adapter)]))

(defmulti -event-msg-handler
          "Multimethod to handle Sente `event-msg`s"
          :id)

(defmethod -event-msg-handler
  :default                                         ; Default/fallback case (no other matching handler)
  [{:as ev-msg :keys [event id ?data ring-req ?reply-fn send-fn]}]
  (log/info "rainmote test ws,,")
  (let [session (:session ring-req)
        uid (:uid session)]
    (log/info "Unhandled event: %s" event)
    (when ?reply-fn
      (?reply-fn {:umatched-event-as-echoed-from-from-server event}))))

(defn event-msg-handler
  "Wraps `-event-msg-handler` with logging, error catching, etc."
  [{:as ev-msg :keys [id ?data event]}]
  (-event-msg-handler ev-msg)                           ; Handle event-msgs on a single thread
  ;; (future (-event-msg-handler ev-msg)) ; Handle event-msgs on a thread pool
  )


(defstate router
          :start (let [{:keys [ch-recv send-fn connected-uids
                               ajax-post-fn ajax-get-or-ws-handshake-fn]}
                       (sente/make-channel-socket-server! (get-sch-adapter) {})]
                   (def ring-ajax-post                ajax-post-fn)
                   (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
                   (def ch-chsk                       ch-recv) ; ChannelSocket's receive channel
                   (def chsk-send!                    send-fn) ; ChannelSocket's send API fn
                   (def connected-uids                connected-uids) ; Watchable, read-only atom

                   (log/info "start websocket router")
                   (sente/start-server-chsk-router!
                     ch-chsk event-msg-handler)
                   )

          :stop (when-let [stop-fn router]
                  (do
                    (log/info "stop websocket, stop *router*")
                    (stop-fn))))

