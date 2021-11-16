(ns verse.core
  (:require
   [reagent.core :as r]
   [reagent.dom :as rdom]
   [goog.events :as events]
   [goog.history.EventType :as HistoryEventType]
   [markdown.core :refer [md->html]]
   [verse.ajax :as ajax]
   [ajax.core :refer [GET POST]]
   [reitit.core :as reitit]
   [clojure.string :as string]
   [verse.three :as three]
   [verse.shader :as shader])
  (:import goog.History))

(defonce session (r/atom {:path :home}))
(defonce mouse (r/atom [0 0]))

(defn cube-page
  []
  (let [three (three/make-perspective-scene)
        material (three/make-basic-material #js {:color 0x00ff00})
        cube (three/make-cube three material)]
    (letfn [(animate []
              (set! (.-x (.-rotation cube)) (+ 0.01 (.-x (.-rotation cube))))
              (set! (.-y (.-rotation cube)) (+ 0.01 (.-y (.-rotation cube))))
              (three/render-scene three)
              (js/requestAnimationFrame animate))]
      (.add (:scene three) cube)
      three
      (set! (.-z (.-position (:camera three))) 5)
      (js/requestAnimationFrame animate)
      [:<>])))

(defn home-page
  []
  (let [three (three/make-shader-scene shader/hello-shader)
        uniforms (:uniforms three)]
    (letfn [(animate [now]
              (let [element (three/dom-element three)
                    width (.-width element)
                    height (.-height element)]
                (.set (.-value (.-u_resolution uniforms)) width height 1)
                (set! (.-value (.-u_time uniforms)) (* now 0.001))
                (three/render-scene three))
              (js/requestAnimationFrame animate))]
      (set!
       (.-onmousemove js/document)
       (fn [e]
         (.set (.-value (.-u_mouse uniforms)) (.-pageX e) (.-pageY e))))
      (js/requestAnimationFrame animate)
      [:<>])))

(def pages
  {:home #'home-page
   :cube #'cube-page})

(defn page
  []
  [(get pages (:path @session))])

;; -------------------------
;; Routes

(def router
  (reitit/router
   [["/" :home]]))

(defn match-route
  [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)
       :data
       :name))

;; -------------------------
;; History
;; must be called after routes have been defined
(defn hook-browser-navigation!
  []
  (doto (History.)
    (events/listen
     HistoryEventType/NAVIGATE
     (fn [^js/Event.token event]
       (swap! session assoc :path (match-route (.-token event)))))
    (.setEnabled true)))

;; -------------------------
;; Initialize app
(defn mount-components
  []
  (swap! session assoc :path (keyword js/path))
  (rdom/render
   [#'page]
   (.getElementById js/document "app")))

(defn init!
  []
  (ajax/load-interceptors!)
  (hook-browser-navigation!)
  (mount-components))
