(ns verse.three)

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
        (when-let [resolution (.-u_resolution uniforms)]
          (.set (.-value resolution) width height 1)))
      (set! (.-width (.-style element)) (str width "px"))
      (set! (.-height (.-style element)) (str height "px"))
      (.setSize (:renderer three) width height false))
    needs-resize?))

(defn make-orthographic-camera
  [width height]
  (js/THREE.OrthographicCamera.
   -1 1 1 -1 -1 1))

(defn make-perspective-camera
  [width height]
  (js/THREE.PerspectiveCamera.
   75 (/ width height) 0.1 1000))

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

(defn make-scene
  [make-camera]
  (let [scene (js/THREE.Scene.)
        width js/window.innerWidth
        height js/window.innerHeight
        camera (make-camera width height)
        uniforms
        (clj->js
         {:u_time {:value 0}
          :u_mouse {:value (js/THREE.Vector2.)}
          :u_resolution {:value (js/THREE.Vector3.)}})
        renderer (js/THREE.WebGLRenderer.)]
    (set! (.-autoClearColor renderer) false)
    (.setSize renderer width height)
    (js/document.body.appendChild (.-domElement renderer))
    {:scene scene
     :camera camera
     :renderer renderer
     :uniforms uniforms}))

(defn make-perspective-scene
  []
  (make-scene make-perspective-camera))

(defn make-shader-scene
  [fragment]
  (let [three (make-scene make-orthographic-camera)
        geometry (js/THREE.PlaneGeometry. 2 2)
        shader (make-shader-material (:uniforms three) fragment)
        plane (js/THREE.Mesh. geometry shader)]
    (.add (:scene three) plane)
    (assoc three :plane plane)))

(defn render-scene
  [three]
  (fit-renderer three)
  (.render
   (:renderer three)
   (:scene three)
   (:camera three)))

