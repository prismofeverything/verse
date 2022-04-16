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

(def mod11
  (fn [n]
    (mod n 11)))

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

(defn find-index
  [find s]
  (first
   (first
    (filter
     (fn [[index el]]
       (find el))
     (map vector (range) s)))))

(defn remove-direction
  [space direction]
  (if-let [index (find-index (partial = direction) space)]
    (vec-remove space index)
    space))

(defn pull-towards
  [direction original]
  (mod6
   (if (= 2 (mod6 (- direction original)))
     (inc original)
     (dec original))))

(defn pull-direction
  [space direction]
  (cond
    (= (count (set space)) 1)
    (let [final-index (dec (count space))]
      (update space final-index (partial pull-towards direction)))
    (= (count (set space)) 2)
    (let [index (find-index (partial lateral-direction? direction) space)]
      (update space index (partial pull-towards direction)))))

(defn apply-direction
  [unsorted-space direction]
  (let [space (mapv mod6 unsorted-space)
        directions (set space)
        direction (mod6 direction)]
    (cond
      (or
       (empty? space)
       (directions direction)
       (and
        (= (count directions) 1)
        (adjacent-direction? (first directions) direction)))
      (conj space direction)

      (some (partial opposite-direction? direction) directions)
      (remove-direction space (opposite-direction direction))

      (some (partial lateral-direction? direction) directions)
      (pull-direction space direction)

      true
      (conj space direction))))

(defn apply-trajectory
  [space trajectory]
  (reduce
   apply-direction
   space
   trajectory))
  
(defn trajectory-sort
  "take unsorted space and sort it according to major and minor axis"
  [unordered-space]
  (let [space (apply-trajectory [] unordered-space)
        directions (set space)]
    (if (or
         (empty? space)
         (= (count directions) 1))
      space
      (let [direction-counts (sort-by last > (frequencies space))
            smaller-count (last (last direction-counts))
            larger-count (last (first direction-counts))
            cycle-length (* 2 smaller-count)
            tail-length (- larger-count smaller-count)
            ordered-directions (map first direction-counts)
            front (take cycle-length (cycle ordered-directions))
            back (repeat tail-length (first ordered-directions))]
        (vec (concat front back))))))

(defn generate-side
  [initial-space direction]
  (rest
   (reverse
    (reduce
     (fn [side n]
       (let [recent-space (first side)
             next-space (apply-direction recent-space direction)]
         (conj side next-space)))
     (list initial-space)
     (repeat (count initial-space) 0)))))

(defn generate-ring
  [ring]
  (if (zero? ring)
    [[]]
    (let [initial-space (vec (repeat ring 0))
          initial-direction 2]
      (apply
       concat
       (rest
        (reverse
         (reduce
          (fn [sides direction]
            (let [recent-side (first sides)
                  recent-axis (last recent-side)
                  next-side (generate-side recent-axis direction)]
              (conj sides next-side)))
          (list (list initial-space))
          (map (fn [x] (mod6 (+ x initial-direction))) (range 6)))))))))


(defn generate-spaces
  [rings]
  (apply
   concat
   (map generate-ring (range rings))))

(defn generate-features
  [rings feature-count buffer]
  (let [all-spaces (generate-spaces rings)
        spaces (filter (fn [space] (> (count space) buffer)) all-spaces)
        feature-spaces (take feature-count (shuffle spaces))
        features (map (fn [space] [space :unknown]) feature-spaces)]
    (into {} features)))

(def example-features
  {:asteroids 13
   :system-four 3
   :system-three 4
   :system-two 5
   :system-one 6
   :black-hole 3
   :artifact 10})

(defn possible-features
  [feature-spec]
  (apply
   concat
   (map
    (fn [type]
      (repeat (last type) (first type)))
    feature-spec)))

(defn new-game
  [rings feature-count feature-spec]
  (let [;; spaces (generate-spaces rings)
        possible (possible-features feature-spec)
        features (generate-features rings feature-count 1)]
    {:rings rings
     ;; :spaces spaces
     :possible possible
     :features features
     :ship 
     {:velocity [0]
      :position []}}))

(defn out-of-bounds?
  [rings space]
  (let [direction-count (count space)]
    (<= direction-count rings)))

;; should apply-direction maintain the sort in all cases?
;;   example space: [2 3 4] 

(defn sort-matters?
  [f space]
  (not
   (= (f (trajectory-sort space))
      (trajectory-sort (f space)))))

(defn move-ship
  [game]
  (let [velocity (get-in game [:ship :velocity])]
    (update-in
     game
     [:ship :position]
     (fn [position]
       (trajectory-sort
        (apply-trajectory position velocity))))))

(defn reveal-feature
  [game space]
  (let [existing (get-in game [:features space])] 
    (cond
      (empty? existing)
      game
      (= existing :unknown)
      ;; shuffle :possible take the first one and remove it from list and replace :unknown with the feature
      game
      )))
