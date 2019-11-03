(ns rainmote.common.antd
  (:require [reagent.core :as r]
            [cljsjs.antd])
  (:require-macros [rainmote.common.macros :refer [def-comp]]))

(def antd js/antd)

(def Breadcrumb (r/adapt-react-class (aget antd "Breadcrumb")))
(def BreadcrumbItem (r/adapt-react-class (aget antd "Breadcrumb" "Item")))
(def Menu (r/adapt-react-class (aget antd "Menu")))
(def MenuItem (r/adapt-react-class (aget antd "Menu" "Item")))
(def MenuSubMenu (r/adapt-react-class (aget antd "Menu" "SubMenu")))
(def Layout (r/adapt-react-class (aget antd "Layout")))
(def LayoutSider (r/adapt-react-class (aget antd "Layout" "Sider")))
(def LayoutHeader (r/adapt-react-class (aget antd "Layout" "Header")))
(def LayoutContent (r/adapt-react-class (aget antd "Layout" "Content")))
(def LayoutFooter (r/adapt-react-class (aget antd "Layout" "Footer")))

(def Row (r/adapt-react-class (aget antd "Row")))
(def Col (r/adapt-react-class (aget antd "Col")))

;; (def-comp "Breadcrumb")
;; (def-comp "Breadcrumb" "Item")
;; (def-comp "Menu")
;; (def-comp "Menu" "Item")
;; (def-comp "Menu" "SubMenu")
;;
;; (def-comp "Layout")
;; (def-comp "Layout" "Sider")
;; (def-comp "Layout" "Header")
;; (def-comp "Layout" "Content")
;; (def-comp "Layout" "Footer")

(def Avatar (r/adapt-react-class (aget antd "Avatar")))

(def Steps (r/adapt-react-class (aget antd "Steps")))
(def Step (r/adapt-react-class (aget antd "Steps" "Step")))

(def Icon (r/adapt-react-class (aget antd "Icon")))

(def IconFont (r/adapt-react-class
                (.createFromIconfontCN
                  (aget js/antd "Icon")
                  #js{"scriptUrl" "//at.alicdn.com/t/font_1387348_euape0js55r.js"})))
