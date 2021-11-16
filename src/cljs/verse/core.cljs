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
   [clojure.string :as string])
  (:import goog.History))

(defonce session (r/atom {:path :home}))

(defn dom-element
  [three]
  (.-domElement
   (:renderer three)))

(defn fit-renderer
  [three]
  (let [element (dom-element three)
        width js/window.innerWidth
        height js/window.innerHeight
        needs-resize? (or
                       (not= (.-width element) width)
                       (not= (.-height element) height))]
    (when needs-resize?
      (when-let [uniforms (:uniforms three)]
        (when-let [resolution (.-iResolution uniforms)]
          (.set (.-value resolution) width height 1)))
      (set! (.-width (.-style element)) (str width "px"))
      (set! (.-height (.-style element)) (str height "px"))
      (.setSize (:renderer three) width height false))
    needs-resize?))

(defn make-orthographic-camera
  []
  (js/THREE.OrthographicCamera. -1 1 1 -1 -1 1))

(defn make-perspective-camera
  [aspect-ratio]
  (js/THREE.PerspectiveCamera. 75 aspect-ratio 0.1 1000))

(defn base-vertex-shader
  []
  "

")

(def hello-shader
  "
#include <common>

uniform vec3 iResolution;
uniform float iTime;

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
  // Normalized pixel coordinates (from 0 to 1)
  vec2 uv = fragCoord/iResolution.xy;

  // Time varying pixel color
  vec3 col = 0.5 + 0.5*cos(iTime+uv.xyx*40.0+vec3(0,2,4));

  // Output to screen
  fragColor = vec4(col,1.0);
}

void main() {
  mainImage(gl_FragColor, gl_FragCoord.xy);
}
")

(defn make-basic-material
  [options]
  (js/THREE.MeshBasicMaterial. options))

(defn make-shader-material
  ([uniforms fragment]
   (js/THREE.ShaderMaterial.
    #js
    {:uniforms uniforms
     :fragmentShader fragment}))

  ([uniforms fragment vertex]
   (js/THREE.ShaderMaterial.
    #js
    {:uniforms uniforms
     :vertexShader vertex
     :fragmentShader fragment})))

(defn make-cube
  [three material]
  (let [geometry (js/THREE.BoxGeometry.)
        cube (js/THREE.Mesh. geometry material)]
    cube))

(defn make-perspective-scene
  []
  (let [scene (js/THREE.Scene.)
        width js/window.innerWidth
        height js/window.innerHeight
        aspect-ratio (/ width height)
        camera (make-perspective-camera aspect-ratio)
        renderer (js/THREE.WebGLRenderer.)]
    (.setSize renderer width height)
    (js/document.body.appendChild (.-domElement renderer))
    {:scene scene
     :camera camera
     :renderer renderer}))

(defn render-scene
  [three]
  (fit-renderer three)
  (.render
   (:renderer three)
   (:scene three)
   (:camera three)))

(defn cube-page
  []
  (let [three (make-perspective-scene)
        material (make-basic-material #js {:color 0x00ff00})
        cube (make-cube three material)]
    (letfn [(animate []
              (js/requestAnimationFrame animate)
              (set! (.-x (.-rotation cube)) (+ 0.01 (.-x (.-rotation cube))))
              (set! (.-y (.-rotation cube)) (+ 0.01 (.-y (.-rotation cube))))
              (render-scene three))]
      (.add (:scene three) cube)
      three
      (set! (.-z (.-position (:camera three))) 5)
      (animate)
      [:<>])))

(defn make-shader-scene
  [fragment]
  (let [scene (js/THREE.Scene.)
        width js/window.innerWidth
        height js/window.innerHeight
        camera (make-orthographic-camera)
        renderer (js/THREE.WebGLRenderer.)
        geometry (js/THREE.PlaneGeometry. 2 2)
        uniforms
        (clj->js
         {:iTime {:value 0}
          :iResolution {:value (js/THREE.Vector3.)}})
        shader (make-shader-material uniforms fragment)
        plane (js/THREE.Mesh. geometry shader)]
    (set! (.-autoClearColor renderer) false)
    (.setSize renderer width height)
    (js/document.body.appendChild (.-domElement renderer))
    (.add scene plane)
    {:scene scene
     :plane plane
     :camera camera
     :renderer renderer
     :uniforms uniforms}))

(defn home-page
  []
  (let [three (make-shader-scene hello-shader)]
    (letfn [(animate [now]
              (let [element (dom-element three)
                    width (.-width element)
                    height (.-height element)]
                (.set (.-value (.-iResolution (:uniforms three))) width height 1)
                (set! (.-value (.-iTime (:uniforms three))) (* now 0.001))
                (render-scene three))
              (js/requestAnimationFrame animate))]
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
