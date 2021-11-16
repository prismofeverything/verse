(ns verse.routes.home
  (:require
   [verse.layout :as layout]
   [clojure.java.io :as io]
   [verse.middleware :as middleware]
   [ring.util.response]
   [ring.util.http-response :as response]))

(defn home-page
  [request]
  (layout/render
   request
   "home.html"
   {:path "home"}))

(defn cube-page
  [request]
  (layout/render
   request
   "home.html"
   {:path "cube"}))

(defn home-routes []
  [""
   {:middleware [middleware/wrap-csrf
                 middleware/wrap-formats]}
   ["/" {:get home-page}]
   ["/cube" {:get cube-page}]])

