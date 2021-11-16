(ns verse.env
  (:require
    [selmer.parser :as parser]
    [clojure.tools.logging :as log]
    [verse.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[verse started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[verse has shut down successfully]=-"))
   :middleware wrap-dev})
