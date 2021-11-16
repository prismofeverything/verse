(ns verse.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [verse.core-test]))

(doo-tests 'verse.core-test)

