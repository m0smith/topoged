(ns topoged.viewer.pedigree.fractal
  (:use quil.core))

(declare branch)

(defn branch* [h neg-h theta name pts]
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
  (text (str "kkk" name) 0 0 (* h 0.8) (/ h 2))

  
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

  
  (branch h theta name pts);       ;; Ok, now call myself to draw two new branches!!
  (pop-matrix);     ;; Whenever we get back here, we "pop" in order to restore the previous matrix state
)

(defn branch [h1 theta name pts]
  (let  [m 0.69
         h (* h1 m)
         neg-h (* h1 (- m))]
    ;;// All recursive functions must have an exit condition!!!!
    ;;// Here, ours is when the length of the branch is 2 pixels or less
    ;(println "H" h neg-h)
    ;;(line 0 0 100 100)
    (when (> h1 6)
      (branch* h neg-h (- theta) (str name "F") (dec pts))
      (branch* h neg-h theta (str name "M") (dec pts))
  )))

(defn setup []
  (set-state! :mouse-position (atom [0 0]))
  (stroke 255)
  (smooth)                          ;;Turn on anti-aliasing
  (frame-rate 30)                    ;;Set framerate to 1 FPS
  (background 0))                 ;;Set the background colour to
                                    ;;  a nice shade of grey.
(defn- draw []
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
    
    (branch line-length theta "C" 17)
  )
)

(defn mouse-moved []
  (let [x (mouse-x) y (mouse-y)]
    ;(println "mouse" x y)
    (reset! (state :mouse-position) [x y])))

(def pedigree-panel-fractal
  (defsketch example                  ;;Define a new sketch named example
    :title "A tree"  ;;Set the title of the sketch
    :target :none
    :mouse-moved mouse-moved
    :setup setup                      ;;Specify the setup fn
    :draw draw                        ;;Specify the draw fn
    :size [700 700]))                  ;;You struggle to beat the golden ratio

(println (class @pedigree-panel-fractal))