(ns topoged.viewer.pedigree.fractal
  (:use
   [seesaw.core :only [grid-panel]]
   [topoged.viewer.common]
   [quil core]
   ))

(declare branch)

(defn branch* [h neg-h theta name pts model id]
  (push-matrix);    ;; Save the current state of transformation (i.e. where are we now)
  (rotate theta);   ;; Rotate by theta

  ;;(rotate (+ theta HALF-PI));   ;; Rotate by theta
  
 ; (rect (* h 0.1) (- (/ h 6)) (* h 0.8) (/ h 3))



  (push-matrix)
  (translate 0 (/ neg-h 2))
  (rotate HALF-PI)
  (text-align :center :center)
  (text-size pts)
  ;(text "J J J")
  (rect-mode :center)
  ;(rect 0 0 (* h 0.8) (/ h 6))
  ;(fill 0)
  ;(rect 0 0 (* h 0.8) (inc pts))
  (text (str "" name) 0 0 (* h 0.8) (+ pts pts))

  
  ;(rect (- (/ h 2)) (- (/ h 6))
  ;      (/ h 2) (/ h 6))
  
;  (text name
;        (- (/ neg-h 6)) (* neg-h 0.1)
;        (/ neg-h 6) (* neg-h 0.9))

;  
   (pop-matrix)
  ;(rect
  ;       (- (/ h 6)) (* h 0.1)
   ;      (/ h 6) (* h 0.9))

  ;(rect 0 (* 0.1 neg-h) 10 (* 0.9 neg-h))
  (line 0,0,0,neg-h);  ;; Draw the branch
  (translate 0,neg-h); ;; Move to the end of the branch

  
  (branch h theta name pts model id);       ;; Ok, now call myself to draw two new branches!!
  (pop-matrix);     ;; Whenever we get back here, we "pop" in order to restore the previous matrix state
  )

(defn next-thetas [theta]
  (if (> theta 0)
    [(- theta) theta]
    [theta (- theta)]))

(defn branch [h1 theta name pts model node]
  (let  [m 0.69
         h (* h1 m)
         neg-h (* h1 (- m))
         [t1 t2] (next-thetas theta)]
    ;;// All recursive functions must have an exit condition!!!!
    ;;// Here, ours is when the length of the branch is 2 pixels or less
    ;(println "H" h neg-h)
    ;;(line 0 0 100 100)
    (when (and node (> h1 6) (> (.getChildCount model node) 1))
      (let [p1 (.getChild model node 0)
            p2 (.getChild model node 1)]
        
        (branch* h neg-h  t1 (->  (m-entities p1) first :name)
                 (dec pts) model p1)
        (branch* h neg-h t2 (-> (m-entities p2) first :name)
                 (dec pts) model p2)
        ))))

(defn setup []
  (set-state! :mouse-position (atom [0 0]))
  (stroke 255)
  (smooth)                          ;;Turn on anti-aliasing
  (frame-rate 10)                    ;;Set framerate to 1 FPS
  (background 0))                 ;;Set the background colour to
                                    ;;  a nice shade of grey.
(defn- draw [model]
  (background 0)
 ;; Let's pick an angle 0 to 90 degrees based on the mouse position
  (let[[X Y] @(state :mouse-position)
       ;n (println "X" X (mouse-x))
       w (width)
       h (height)
       ;n (println "Hieght" h)
       a  (* (/ X  w) 90.0)
       ;; Convert it to radians
       theta  (radians a)
       line-length (* h 0.4)];

    ;; Start the tree from the bottom of the screen
    
    (translate (/ w 2) h);
    
    ;; Draw a line 60 pixels
    
    (line 0,0,0,(- line-length));
    
    ;; Move to the end of that line
    
    (translate 0,(- line-length));
    
    ;; Start the recursive branching!
    
    (branch line-length theta "C" 16 model (.getRoot model))
  )
)

(defn mouse-moved []
  (let [x (mouse-x) y (mouse-y)]
    ;(println "mouse" x y)
    (reset! (state :mouse-position) [x y])))


(def pedigree-panel-fractal (grid-panel))

(defn build-pedigree-panel-fractal [id]
  (let [model (load-model id m-parents-of)]
    (defsketch example                  ;;Define a new sketch named example
      :title "A tree"  ;;Set the title of the sketch
      :target :none
      :mouse-moved mouse-moved
      :setup setup                       ;;Specify the setup fn
      :draw (partial draw model)         ;;Specify the draw fn
      :size [700 700])))                  ;;You struggle to beat the golden ratio

