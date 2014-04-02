(ns topoged.viewer.pedigree.std
  (:use [quil.core]))

(def p-width 100)
(def p-height 100)

(defn setup [] 
  (smooth)         
  (frame-rate 10)   
  (background 200))

(defn draw-lines [scle tx ty]
  (push-matrix)
  (translate tx ty)
  (scale scle)
  (line 0 p-height p-width p-height)
  (line p-width 0 p-width (* 2  p-height))
  (pop-matrix))

(defn draw []
  ;(line 50 50 80 80)
  (let [x 50 y 200]
    (draw-lines 1.0  x y)
    (draw-lines 0.8 150 (+ y (* 1.2 p-height)))
    (draw-lines 0.64 (- 250 (* 0.4 50)) (+ y (* 2.15 p-height)))
    (draw-lines 0.64 (- 250 (* 0.4 50)) (+ y (* 0.55 p-height)))
    (draw-lines 0.8 150 (- y (* 0.8 p-height)))))
  

(defsketch example           
  :title "Standard Pedigree" 
  :setup setup               
  :draw draw                
  :size [800 600])  


