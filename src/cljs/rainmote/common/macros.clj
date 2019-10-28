(ns rainmote.common.macros
  (:require [clojure.string]))

(defmacro def-comp [& comps]
  `(def ~(-> comps clojure.string/join symbol)
     (reagent.core/adapt-react-class
       (goog.object/getValueByKeys js/antd ~@comps))))
