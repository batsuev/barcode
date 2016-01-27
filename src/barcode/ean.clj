(ns barcode.ean
  (:require [clojure.tools.logging :as log]))

;; port of php-barcode ean encoder

(def ^:private digits [3211 2221 2122 1411 1132 1231 1114 1312 1213 3112])
(def ^:private mirror ["000000" "001011" "001101" "001110" "010011" "011001" "011100"
                       "010101" "010110" "011010"])
(def ^:private guards ["9a1a" "1a1a1" "a1a"])

(defn- generate-ean-sum [ean]
  (loop [even true
         esum 0
         osum 0
         i (-> ean count dec)]
    (if (< i 0)
      (mod (- 10 (mod (+ (* 3 esum) osum) 10)) 10)
      (let [val (-> ean (nth i) str read-string)]
        (if-not (number? val)
          (recur (not even) esum osum (dec i))
          (if even
            (recur (not even) (+ esum val) osum (dec i))
            (recur (not even) esum (+ osum val) (dec i))))))))

(defn- check-isbn [ean encoding]
  (if (and (= encoding "ISBN")
           (not (re-find #"^978" ean)))
    (str 978 ean)
    ean))

(defn- check-encoding [ean encoding]
  (if (re-find #"^978" ean)
    "ISBN"
    encoding))

(defn- encode-bars [ean]
  (log/info (str "encoding " ean))
  (str (reduce (fn [res idx]
                 (let [ean-at-idx (-> ean (nth idx) str read-string)
                       s (-> digits (nth ean-at-idx) str)]
                   (let [res (if (and (< idx 7)
                                      (= (-> mirror
                                             (nth (-> ean first str read-string))
                                             (nth (dec idx))
                                             str
                                             read-string) 1))
                               (str res (reduce str (reverse s)))
                               (str res s))]
                     (if (= idx 6)
                       (str res (nth guards 1))
                       res))))
               (nth guards 0)
               (range 1 13))
       (nth guards 2)))

(defn- encode-text [ean]
  (loop [pos 0
         text ""
         a 0]
    (if (= 13 a)
      text
      (recur (case a
               0 (+ pos 12)
               6 (+ pos 12)
               (+ pos 7))
             (if (pos? a)
               (str text " " pos ":12:" (-> ean (nth a) str))
               (str text pos ":12:" (-> ean (nth a) str)))
             (inc a)))))

(defn- encode-ean [ean encoding]
  (let [ean-summ (generate-ean-sum ean)
        ean (str ean ean-summ)]
    {:encoding encoding
     :bars (encode-bars ean)
     :text (encode-text ean)}))

(defn encode
  ([ean] (encode ean "EAN-13"))
  ([ean encoding]
   (let [ean (clojure.string/trim ean)
         encoding (if (some? encoding) (clojure.string/upper-case encoding))]
     (if (re-find #"[^0-9]" ean)
       (do (log/info (str "Invalid ean code: " ean))
           false)
       (let [ean (check-isbn ean encoding)
             encoding (check-encoding ean encoding)]
         (if (or (< (count ean) 12)
                 (> (count ean) 13))
           (do (log/info (str "Invalid ean code length: " ean))
               nil)
           (encode-ean (subs ean 0 12) encoding)))))))
