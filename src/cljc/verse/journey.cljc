(ns verse.journey)

(defn hex-positions
  [rings distance]
  (let [top-half
        (map
         (fn [ring]
           (let [spaces (+ rings ring)
                 y (- (dec rings) ring)
                 left-side (int (Math/floor (* (dec spaces) 0.5)))]
             (map
              (fn [space]
                [(- space left-side) y])
              (range spaces))))
         (range rings))
        bottom-half
        (map
         (fn [spaces]
           (map
            (fn [[x y]]
              [x (* -1 y)])
            spaces))
         (take (dec rings) top-half))]
    (concat top-half (reverse bottom-half))))


