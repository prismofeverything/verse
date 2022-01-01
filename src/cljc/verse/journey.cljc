(ns verse.journey)

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

(defn mod6
  [n]
  (mod n 6))

(defn valid-space?
  [space]
  (let [directions (set space)
        skew (count directions)]
    (and
     (<= skew 2)
     (if (= skew 2)
       (let [side (mod6 (- (first skew) (last skew)))]
         (#{1 5} side))
       true))))

(defn opposite
  [n]
  (mod6 (+ 3 n)))

(defn opposite?
  [a b]
  (= 3 (mod6 (- a b))))

(defn vec-remove
  [coll index]
  (into
   (subvec coll 0 index)
   (subvec coll (inc index))))

(defn remove-direction
  [space direction]
  (if (some #{direction} space)
    (let [index (.indexOf space direction)]
      (vec-remove space index))
    space))

(defn apply-direction
  [space direction]
  (let [directions (set space)]
    (cond
      ;; deal with spaces in only one direction that add a new adjacent direction
      (directions direction) (conj space direction)
      (some (partial opposite? direction) directions) (remove-direction space (opposite direction))
      ;; deal with spaces that have elements that are two away from direction
      )))

(defn generate-board
  [rings])


