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
  [space direction]
  (if (valid-space? space)
    (let [directions (set space)
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
        (pull-direction space direction)))
    (throw (Exception. (str "not valid space: " space)))))

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

(defn generate-ring
  [ring]
  (if (zero? ring)
    [[]]))
  

(defn generate-spaces
  [rings]
  )

(defn generate-features
  [rings feature-count]
  )

(defn new-game
  [rings]
  (let [spaces (generate-spaces rings)]
    {:rings rings
     :spaces spaces
     :ship
     :feature
     {:velocity [0]
      :position []}}))

(defn move-ship
  [game]
  (let [velocity (get-in game [:ship :velocity])]
    (update-in
     game
     [:ship :position]
     (fn [position]
       (trajectory-sort
        (apply-trajectory position velocity))))))
