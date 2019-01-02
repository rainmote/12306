(ns rainmote.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [rainmote.core-test]))

(doo-tests 'rainmote.core-test)

