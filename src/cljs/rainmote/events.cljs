(ns rainmote.events
  (:require [re-frame.core :as rf]))

;;dispatchers

(rf/reg-event-db
  :initial-db
  (fn [_ _]
    {:sider-collapsed false}))

(rf/reg-event-db
  :navigate
  (fn [db [_ page]]
    (assoc db :nav-path page)))

(rf/reg-sub
  :nav-path
  (fn [db _]
    (:nav-path db)))

(rf/reg-event-db
 :set-breadcrumb
 (fn [db [_ data]]
   (assoc db :breadcrumb data)))

(rf/reg-sub
 :breadcrumb
 (fn [db _]
   (:breadcrumb db)))

(rf/reg-event-db
 :set-history-obj
 (fn [db [_ obj]]
   (assoc db :history-obj obj)))

(rf/reg-sub
 :history-obj
 (fn [db _] (:history-obj db)))

(rf/reg-event-db
  :set-sider-collapsed
  (fn [db [_ v]]
    (if v
      (assoc db :sider-collapsed v)
      (update db :sider-collapsed not))))

(rf/reg-sub
  :sider-collapsed
  (fn [db _] (:sider-collapsed db)))