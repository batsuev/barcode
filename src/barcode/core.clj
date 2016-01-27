(ns barcode.core
  (:require [clojure.tools.logging :as log]
            [barcode.drawer :as drawer]
            [barcode.ean :as ean])
  (:import [barcode BarCode BarCodeResponse]
           java.io.ByteArrayInputStream))

(defn- precheck-code [code]
  (cond
    (nil? code) "123456789012"
    :else code))

(defn- delete-ean13-checksumm [code encoding]
  (if (and (= encoding "ean")
           (= (count code) 13))
    (subs code 0 12)
    code))

(defn- is-ean-encoding [code encoding]
  (let [code-len (count code)]
    (or (and (= encoding "ean")
             (or (= code-len 12)
                 (= code-len 13)))
        (and (= encoding "isbn")
             (or (= code-len 9)
                 (= code-len 10)
                 (and (re-find #"^978" code) (= code-len 12))
                 (= code-len 13)))
        (and (or (nil? encoding)
                 (= encoding "any"))
             (re-find #"^[0-9]{12,13}$" code)))))

(defn get-info-ean [code encoding]
  (let [res (ean/encode code encoding)]
    (BarCodeResponse. (:encoding res)
                      (:text res)
                      (:bars res))))

(defn get-info-gnu-barcode [code encoding]
  (let [code code
        encoding (-> encoding
                     (or "any")
                     (clojure.string/replace #"[|\\]" "_")
                     clojure.string/upper-case)]
    (BarCode/Create code encoding)))

(defn get-info [code encoding]
  (if (is-ean-encoding code encoding)
    (get-info-ean code encoding)
    (get-info-gnu-barcode code encoding)))

(defn generate [code encoding scale mode]
  (let [raw-code code
        code (precheck-code code)
        scale (if scale (-> scale str read-string) 2)
        encoding (-> encoding (or "any") clojure.string/lower-case)
        info (get-info code encoding)
        mode (-> mode (or "png") clojure.string/lower-case)]
    (when (some? info)
      (log/info (str "code: " code))
      (log/info (str "bars " (.getPartial info)))
      (log/info (str "text" (.getTextInfo info))))
    (try
      (if info
        {:body (ByteArrayInputStream. (drawer/draw-barcode info scale mode))
         :status 200
         :header {"Content-Type" (drawer/content-type mode)}}
        (do
          (log/info (str "Failed to generate barcode, code " raw-code " ,info " info ", mode " mode ", scale " scale ", encoding " encoding))
          {:body "" :status 500}))
      (catch Exception e
        (log/error e "Got exception in barcode generation")
        (log/info (str "Failed to show barcode: " raw-code))
        (throw e)))))
