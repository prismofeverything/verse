(ns verse.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[verse started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[verse has shut down successfully]=-"))
   :middleware identity})
