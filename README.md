# barcode

A Clojure library for drawing barcodes based on gnu-barcode (using JNI) and php-barode.

## Installation
lein dependency:

    [batsuev/barcode "0.1.0"]
    
project.clj:

    :jvm-opts ["-Dbarcode.so=path-to-barcode.so"]
    
## Building barcode.so

    cd barcode
    ./configure
    make

## Usage with ring

    (ns your-project 
      (:require [barcode.core :as barcode] ... ))
      
    (defn- show-barcode [request]
      (let [code (-> request :params (get "code"))]
        (barcode/generate code "ANY" 2 "png")))
    
    (defroutes app-routes
      (GET "/barcode" [] show-barcode))
      
## Write barcode to file

    (ns your-project
      (:require [barcode.core :as barcode]
                [clojure.java.io :as io]))
                
    (io/copy (:body (barcode/generate "123456" "ANY" 2 "png")) (io/file "test.png"))

## License

Copyright © 2016 

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
