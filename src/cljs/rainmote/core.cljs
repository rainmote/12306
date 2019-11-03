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
            [rainmote.common.antd :as ant]
            [rainmote.ticket-tool.dashboard.ui]
            [rainmote.ticket-tool.add.ui]
            [rainmote.slave.add.ui]
            [rainmote.intelligence.tor.ui])
  (:import goog.History))

(defn home-page []
  [:h1 "home page..."])

(defn about-page []
  [:h1 "about page..."])

(defn menu-data []
  [{:type     :menu
    :nav-path []
    :url      "/home"
    :content  #'home-page
    :icon     "icon-home"
    :name     "首页"}
   {:type     :submenu
    :nav-path [:intelligence]
    :title    {:type :menu
               :icon "icon-global"
               :name "情报"}
    :data     [{:type     :menu
                :nav-path [:intelligence :tor]
                :content  #'rainmote.intelligence.tor.ui/page
                :url      "/intelligence/tor"
                :name     "Tor"}]}
   {:type     :submenu
    :nav-path [:ticket-tool]
    :title    {:type :menu
               :icon "icon-huochezhan"
               :name "抢票工具"}
    :data     [{:type     :menu
                :nav-path [:ticket-tool :dashboard]
                :content  #'rainmote.ticket-tool.dashboard.ui/page
                :url      "/ticket-tool/dashboard"
                :name     "大盘"}
               {:type     :menu
                :nav-path [:ticket-tool :add]
                :content  #'rainmote.ticket-tool.add.ui/page
                :url      "/ticket-tool/add"
                :name     "新建任务"}]}
   {:type     :submenu
    :nav-path [:slave]
    :title    {:type :menu
               :icon "icon-jiqun-mianxing"
               :name "节点管理"}
    :data     [{:type     :menu
                :nav-path [:slave :add]
                :content  #'rainmote.slave.add.ui/page
                :url      "/slave/add"
                :name     "新增节点"}]}
   {:type     :menu
    :nav-path [:about]
    :url      "/about"
    :content  #'about-page
    :icon     "icon-guanyuwomen"
    :name     "关于"}])

(defn get-menu-nav-path-data [data]
  "Convert menu-data to {[:about] {:type xx :url xx}}"
  (let [{:keys [menu submenu]} (group-by :type data)
        submenu-fn (fn [x]
                     (-> (get-menu-nav-path-data (:data x))
                         (merge,,, {(:nav-path x) (:title x)})))]
    (merge (reduce #(merge %1 (submenu-fn %2)) {} submenu)
           (reduce #(assoc %1 (:nav-path %2) %2) {} menu))))

(defn nav-path-> [nav-path k]
  (as-> (get-menu-nav-path-data (menu-data)) data
        ;; [:home] <=> []
        (assoc data [:home] (get data []))
        (get-in data [nav-path k])))

(defn gen-page-breadcrumb [nav-path]
  (->> (take (count nav-path) (range))
       (map #(take (+ % 1) nav-path),,,)
       (mapv (fn [x]
               (let [p (->> x rest (apply vector))]
                 (-> {}
                     (assoc,,, :title (nav-path-> p :name))
                     (assoc,,, :href (nav-path-> p :href))))),,,)
       #_(#(timbre/spy :info %))))

(defn distinct-consequtive [s]
  (map first (partition-by identity s)))

(defn header [nav-path]
  [ant/LayoutHeader {:style {:background "#FFF"
                             :padding 0}}
    [ant/Icon {:className "trigger"
              :style {:font-size "20px"
                      :vertical-align :middle
                      :padding-left 16}
              ;;:theme     "filled"
              :type      (if @(rf/subscribe [:sider-collapsed])
                           "menu-unfold"
                           "menu-fold")
              :onClick   (fn [_]
                           (rf/dispatch [:set-sider-collapsed]))}]
   [:div {:style {:float "right"}}
    [ant/Row {:type "flex" :justify "space-around"
              :style {:padding "0 20 0 20"}}
     [ant/Col {:span 8 :style {:padding "0 12px"}}
      [ant/Avatar "Admin"]]
     [ant/Col {:span 8 :style {:padding "0 12px"}}
      [ant/Avatar "Admin"]]
     [ant/Col {:span 8 :style {:padding "0 12px"}}
      [ant/Avatar "Admin"]
      ]]
    ]])

(defn menu-icon [icon]
  (when icon
    [ant/IconFont {:type  icon
                   :style {:font-size      "18px"
                           :vertical-align :middle}}]
    #_[ant/Icon {:type  icon
                 :theme "outlined"
                 :style {:fontSize "15px"}}]))

(defn gen-menu [data]
  (for [it data]
    (condp = (:type it)
      :menu
      [ant/MenuItem {:key (:url it)}
       (menu-icon (:icon it))
       [:span #_{:style {:font-size "16px"}}
        (:name it)]]

      :submenu
      [ant/MenuSubMenu
       {:key   (hash it)
        :title (r/as-element
                 [:span (menu-icon (-> it :title :icon))
                  [:span #_{:style {:font-size "16px"}}
                   (-> it :title :name)]])}
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
  (let [sider-collapsed (rf/subscribe [:sider-collapsed])]
    [ant/LayoutSider {:collapsible      true
                      :defaultCollapsed false
                      :trigger          nil
                      :onCollapse       (fn [state type]
                                          (rf/dispatch [:set-sider-collapsed state]))
                      :collapsed        @sider-collapsed}
     [:h2 {:align "center"
           :style {:color       "#1890FF"
                   :padding-top 20}}
      (if @sider-collapsed
        "R"
        "RainMote"
        )]
     [ant/Menu {:theme   "dark"
                :mode    "inline"
                :onClick switch-page}
      (gen-menu (menu-data))]]))

(defn content [nav-path]
  (let [breadcrumb (-> (cons :home nav-path)
                       distinct-consequtive
                       gen-page-breadcrumb)]
    [ant/LayoutContent
     [:div {:style {;;:background "#FFF"
                    :height 30}}
      [ant/Breadcrumb {:style {:padding "14px 16px"}}
       (for [item breadcrumb]
         [ant/BreadcrumbItem {:key (hash item)}
          (let [href (-> item :href)
                title (-> item :title)]
            (if href
              [:a {:href href} title]
              title))])]]
     [:div {:style {:background "#FFFFFF"
                    :margin     "16px 16px"
                    :minHeight  1080
                    :padding    24}}
      (when nav-path
        ((nav-path-> nav-path :content)))]]))

(defn footer []
  [ant/LayoutFooter {:style {:textAlign "center"}}
   "Copyright "
   [ant/IconFont {:type  "icon-round-copyright-px"
                  :style {:vertical-align :middle}}]
   " 2019 rainmote"])

(defn gen-page []
  (let [nav-path (rf/subscribe [:nav-path])]
    [ant/Layout {:style {:height "100%"}}
     (sider)
     [ant/Layout
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
                               :as   params}
                    (rf/dispatch (->> (clojure.string/split * #"/")
                                      (mapv keyword,,,)
                                      (vector :navigate,,,))))

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
  (rf/dispatch-sync [:initial-db])
  (ajax/load-interceptors!)
  (rf/dispatch-sync [:set-history-obj (hook-browser-navigation!)])
  (mount-components))
