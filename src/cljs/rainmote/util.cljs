(ns rainmote.util
  (:require [cljsjs.antd]
            [reagent.core :as r]
            ))

(def icon-font
  (r/adapt-react-class
    (.createFromIconfontCN (aget js/antd "Icon")
      {:scriptUrl "//at.alicdn.com/t/font_1387348_cq7c6xnnpir.js"})))