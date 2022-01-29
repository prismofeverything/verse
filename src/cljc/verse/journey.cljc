(ns verse.journey)

;; this is leftover from the old way

(defn shift-expand
  [y left-side space]
  [(- space left-side) y])
  
(defn hex-positions
  [num-rings distance]
  (let [top-half
        (map
         (fn [ring]
           (let [num-spaces (+ num-rings ring)
                 y (- (dec num-rings) ring)
                 left-side (int (Math/floor (* (dec num-spaces) 0.5)))]
             (map
              (partial shift-expand y left-side)
              (range num-spaces))))
         (range num-rings))
        bottom-half
        (map
         (fn [spaces]
           (map
            (fn [[x y]]
              [x (* -1 y)])
            spaces))
         (take (dec num-rings) top-half))]
    (concat top-half (reverse bottom-half))))

;; deriving adjacencies from the name itself

(defn mod6
  [n]
  (mod n 6))

(defn opposite-direction
  [n]
  (mod6 (+ 3 n)))

(defn direction-difference?
  [differences a b]
  (let [between (mod6 (- a b))]
    (some
     (partial = between)
     differences)))

(def opposite-direction?
  (partial direction-difference? [3]))

(def lateral-direction?
  (partial direction-difference? [2 4]))

(def adjacent-direction?
  (partial direction-difference? [1 5]))

(defn valid-space?
  [space]
  (let [directions (set space)
        skew (count directions)]
    (and
     (<= skew 2)
     (if (= skew 2)
       (adjacent-direction? (first directions) (last directions))
       true))))

(defn vec-remove
  [v index]
  (into
   (subvec v 0 index)
   (subvec v (inc index))))

(defn remove-direction
  [space direction]
  (if (some #{direction} space)
    (let [index (.indexOf space direction)]
      (vec-remove space index))
    space))

(defn trajectory-sort
  "take unsorted space and sort it according to major and minor axis"
  [space]
  space)

(defn apply-direction
  [space direction]
  (let [directions (set space)]
    (cond
      (or
       (directions direction)
       (and
        (= (count directions) 1)
        (adjacent-direction? (first directions) direction)))
      (conj space direction)

      (some (partial opposite-direction? direction) directions)
      (remove-direction space (opposite-direction direction))

      ;; deal with spaces that have elements that are two away from direction
      
      )))

(defn generate-spaces
  [rings])


