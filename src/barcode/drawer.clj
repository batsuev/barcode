(ns barcode.drawer
  (:require [clojure.tools.logging :as log]
            [clojure.java.io :refer [file]])
  (:import [java.awt Color Font BasicStroke RenderingHints]
           [java.io ByteArrayOutputStream FileInputStream]
           java.awt.image.BufferedImage
           javax.imageio.ImageIO))

(def ^:private bar-color Color/BLACK)
(def ^:private bg-color Color/WHITE)
(def ^:private text-color Color/BLACK)

(defn- get-font-path []
  (System/getProperty "barcode.font"))

(log/info (str "Using font at " (get-font-path)))

(def ^:private font (if (and (get-font-path)
                             (.exists (file (get-font-path))))
                      (Font/createFont Font/TRUETYPE_FONT (FileInputStream. (get-font-path)))
                      (Font. "Arial" Font/BOLD 14)))

(defn- draw-background [graphics w h]
  (doto graphics
    (.setColor bg-color)
    (.fillRect 0 0 w h)))

(defn- calc-width [bars scale space]
  (loop [x-pos 0
         width true
         bar (first bars)
         bars (rest bars)]
    (if-not bar
      (+ x-pos (* space 2))
      (if width
        (recur (+ x-pos (* (-> bar str read-string) scale)) false (first bars) (rest bars))
        (let [value (if (re-find #"[a-z]" (str bar))
                      (- (int bar) (int \a) -1)
                      (-> bar str read-string))]
          (recur (+ x-pos (* value scale)) false (first bars) (rest bars)))))))

(defn- draw-bars [graphics bars scale space h]
  (.setColor graphics bar-color)
  (.setStroke graphics (BasicStroke. 1))
  (let [h (- h (* scale 2))
        small-h (- h (* scale 10))
        big-h (- h space)]
    (loop [width true
           x-pos space
           bar (first bars)
           bars (rest bars)]
      (when bar
        (if width
          (recur false
                 (+ x-pos (* (let [val (-> bar str read-string)]
                               (if (number? val)
                                 val
                                 0))
                             scale))
                 (first bars) (rest bars))
          (let [is-sym (re-find #"[a-zA-Z]" (str bar))
                bar-val (if is-sym
                          (- (int bar) (int \a) -1)
                            (-> bar str read-string))
                h (if is-sym big-h small-h)]
            (.fillRect graphics (int x-pos) space (dec (* bar-val scale)) h)
            (.drawRect graphics (int x-pos) space (dec (* bar-val scale)) h)
            (recur true
                   (+ x-pos (* bar-val scale))
                   (first bars) (rest bars))))))))

(defn- draw-texts [graphics texts scale space h]
  (.setColor graphics text-color)
  (let [texts (if (some? texts)
                (clojure.string/split texts #" "))]
    (loop [idx 0
           text (first texts)
           texts (rest texts)]
      (when (some? text)
        (if (clojure.string/blank? text)
          (recur (inc idx) (first texts) (rest texts))
          (let [info (clojure.string/split text #":")
                font-size (* scale
                             (/ (-> (nth info 1) read-string)
                                1.8))
                font-height (- h (/ font-size 2.7) -2)
                f (.deriveFont font (float (+ font-size (* 2.5 scale))))]
            (.setFont graphics f)
            (.drawString graphics
                         (-> info (nth 2) str)
                         (float (+ space (* scale (-> info first read-string)) 2))
                         (float font-height))
            (recur (inc idx) (first texts) (rest texts))))))))

(defn- calc-height [scale]
  (* scale 60))

(defn draw-barcode [info scale mode]
  (let [scale (if (< scale 1) 2 scale)
        space (* scale 2)
        bars (.getPartial info)

        w (calc-width bars scale space)
        h (calc-height scale)

        img (BufferedImage. w h BufferedImage/TYPE_INT_ARGB)
        graphics (.getGraphics img)]
    (doto (.getGraphics img)
      (.setRenderingHint RenderingHints/KEY_TEXT_ANTIALIASING
                         RenderingHints/VALUE_TEXT_ANTIALIAS_ON)
      (draw-background w h)
      (draw-bars bars scale space h)
      (draw-texts (.getTextInfo info) scale space h))
    (let [output (ByteArrayOutputStream.)]
      (ImageIO/write img (or mode "png") output)
      (.toByteArray output))))

(defn content-type [mode]
  (case mode
    "jpg" "image/jpeg"
    "image/png"))
