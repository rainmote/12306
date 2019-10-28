(ns rainmote.common.antd
  (:require [reagent.core :as r]
            [cljsjs.antd])
  (:require-macros [rainmote.common.macros :refer [def-comp]]))

(def antd js/antd)

#_(def Breadcrumb (r/adapt-react-class (aget antd "Breadcrumb")))
#_(def BreadcrumbItem (r/adapt-react-class (aget antd "Breadcrumb" "Item")))
#_(def Menu (r/adapt-react-class (aget antd "Menu")))
#_(def MenuItem (r/adapt-react-class (aget antd "Menu" "Item")))
#_(def MenuSubMenu (r/adapt-react-class (aget antd "Menu" "SubMenu")))
#_(def Layout (r/adapt-react-class (aget antd "Layout")))
#_(def LayoutSider (r/adapt-react-class (aget antd "Layout" "Sider")))
#_(def LayoutContent (r/adapt-react-class (aget antd "Layout" "Content")))
#_(def LayoutFooter (r/adapt-react-class (aget antd "Layout" "Footer")))

#_(def Icon (r/adapt-react-class (aget antd "Icon")))

(def-comp "Breadcrumb")
(def-comp "Breadcrumb" "Item")
(def-comp "Menu")
(def-comp "Menu" "Item")
(def-comp "Menu" "SubMenu")

(def-comp "Layout")
(def-comp "Layout" "Sider")
(def-comp "Layout" "Header")
(def-comp "Layout" "Content")
(def-comp "Layout" "Footer")

(def-comp "Avatar")

(def Steps (r/adapt-react-class (aget antd "Steps")))
(def Step (r/adapt-react-class (aget antd "Steps" "Step")))

(def-comp "Icon")

(def IconFont (r/adapt-react-class
                (.createFromIconfontCN
                  (aget js/antd "Icon")
                  #js{"scriptUrl" "//at.alicdn.com/t/font_1387348_euape0js55r.js"})))
