(ns rainmote.core
  (:require [baking-soda.core :as b]
            [reagent.core :as r]
            [re-frame.core :as rf]
            [goog.events :as events]
            [goog.history.EventType :as HistoryEventType]
            [markdown.core :refer [md->html]]
            [ajax.core :refer [GET POST]]
            [rainmote.ajax :as ajax]
            [rainmote.events]
            [secretary.core :as secretary]
            [taoensso.sente :as sente]
            [taoensso.timbre :as timbre]
            [antizer.reagent :as ant]
            [clj-antd-pro.reagent :as antp]
            [rainmote.ticket-tool.online.ui]
            [rainmote.intelligence.tor.ui])
  (:import goog.History))

(defn home-page []
  [:h1 "home page..."])

(defn about-page []
  [:h1 "about page..."])

(defn menu-data []
  [{:type :menu
    :nav-path []
    :url "/home"
    :content #'home-page
    :icon "home"
    :name "首页"}
   {:type :submenu
    :nav-path [:intelligence]
    :title {:type :menu
            ;;:icon "radar-chart"
            :icon "global"
            :name "情报"}
    :data [{:type :menu
            :nav-path [:intelligence :tor]
            :content #'rainmote.intelligence.tor.ui/page
            :url "/intelligence/tor"
            :name "Tor"}]}
   {:type :submenu
    :nav-path [:ticket-tool]
    :title {:type :menu
            :icon "tool"
            :name "抢票工具"}
    :data [{:type :menu
            :nav-path [:ticket-tool :online]
            :content #'rainmote.ticket-tool.online.ui/page
            :url "/ticket-tool/online"
            :name "在线抢购"}]}
   {:type :menu
    :nav-path [:about]
    :url "/about"
    :content #'about-page
    :icon "book"
    :name "关于"}])

(defn get-menu-nav-path-data [data]
  "Convert menu-data to {[:about] {:type xx :url xx}}"
  (let [{:keys [menu submenu]} (group-by :type data)
        submenu-fn (fn [x]
                     (-> (get-menu-nav-path-data (:data x))
                         (merge ,,, {(:nav-path x) (:title x)})))]
    (merge (reduce #(merge %1 (submenu-fn %2)) {} submenu)
           (reduce #(assoc %1 (:nav-path %2) %2) {} menu))))

(defn nav-path-> [nav-path k]
  (as-> (get-menu-nav-path-data (menu-data)) data
        ;; [:home] <=> []
        (assoc data [:home] (get data []))
        (get-in data [nav-path k])))

(defn gen-page-breadcrumb [nav-path]
  (->> (take (count nav-path) (range))
       (map #(take (+ % 1) nav-path) ,,,)
       (mapv (fn [x]
              (let [p (->> x rest (apply vector))]
                (-> {}
                  (assoc ,,, :title (nav-path-> p :name))
                  (assoc ,,, :href (nav-path-> p :href)))))
            ,,,)
       #_(#(timbre/spy :info %))))

(defn distinct-consequtive [s]
  (map first (partition-by identity s)))

(defn header [nav-path]
  (let [breadcrumb (-> (cons :home nav-path)
                       distinct-consequtive
                       gen-page-breadcrumb)]
    [:div {:style {:background "#FFF"
                   :height 50}}
      [ant/breadcrumb {:style {:padding "14px 16px"}}
       (for [item breadcrumb]
         [ant/breadcrumb-item {:key (hash item)}
          (let [href (-> item :href)
                title (-> item :title)]
            (if href
              [:a {:href href} title]
              title))])]]))

(defn menu-icon [icon]
  (when icon
    [ant/icon {:type icon
               :theme "outlined"
               :style {:fontSize "15px"}}]))

(defn gen-menu [data]
  (for [it data]
    (condp = (:type it)
      :menu
      [ant/menu-item {:key (:url it)}
       (menu-icon (:icon it))
       [:span (:name it)]]

      :submenu
      [ant/menu-sub-menu
       {:key (hash it)
        :title (r/as-element
                [:span (menu-icon (-> it :title :icon))
                 [:span (-> it :title :name)]])}
       (gen-menu (:data it))]

      (timbre/error "Parse menu error: " it))))

(defn switch-page [e]
  (let [path (str "/page" (aget e "key"))]
    (if (secretary/locate-route path)
      (do
        (. @(rf/subscribe [:history-obj]) (setToken path))
        (secretary/dispatch! path))
      (timbre/error "Not found page:" path))))

(defn sider []
  [ant/layout-sider {:collapsible true}
   [:h2 {:align "center"
         :style {:color "#1890FF"
                 :padding-top 20}}
    "RainMote"]
   [ant/menu {:theme "dark"
              :mode "inline"
              :onClick switch-page}
    (gen-menu (menu-data))]])

(defn content [nav-path]
  [ant/layout-content
   [:div {:style {:background "#FFFFFF"
                  :margin "16px 16px"
                  :minHeight 360
                  :padding 24}}
    (when nav-path
      ((nav-path-> nav-path :content)))]])

(def copyright
  (r/as-element
   [:div "Copyright" [ant/icon {:type "copyright"}] "雨中尘埃"]))

(defn footer []
  [ant/layout-footer {:style {:textAlign "center"}}
   "Copyright "
   [ant/icon {:type "copyright"}]
   " 2018 rainmote"])

(defn gen-page []
  (let [nav-path (rf/subscribe [:nav-path])]
    [ant/layout {:style {:height "100%"}}
     (sider)
     [ant/layout
      (header @nav-path)
      (content @nav-path)
      (footer)]]))

;; -------------------------
;; Routes

(secretary/set-config! :prefix "#")

(secretary/defroute "/" []
  (rf/dispatch [:navigate [:home]]))

(secretary/defroute "/page" []
                    (rf/dispatch [:navigate [:home]]))

(secretary/defroute "/page/*" {:keys [* query-params]
                               :as params}
  (rf/dispatch (->> (clojure.string/split * #"/")
                    (mapv keyword ,,,)
                    (vector :navigate ,,,))))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (secretary/dispatch! (.-token event))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components []
  (rf/clear-subscription-cache!)
  (r/render [#'gen-page] (.getElementById js/document "app")))

(defn init! []
  (ajax/load-interceptors!)
  (rf/dispatch-sync [:set-history-obj (hook-browser-navigation!)])
  (mount-components))
