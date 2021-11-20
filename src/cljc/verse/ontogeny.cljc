(ns verse.ontogeny)

(defn generate-seed
  []
  {:positions
   {:a [1.0 1.0 1.0]
    :b [2.0 -1.0 3.0]
    :c [3.0 -5.0 1.0]}
   :link
   {:a [:b :c]
    :b [:c]
    :c [:a]}})

(defn move-nodes
  [positions])

(defn evolve-seed
  [seed]
  (move-nodes (:positions seed)))
