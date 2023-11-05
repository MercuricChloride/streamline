(ns streamline.proto-helpers
    (:require [streamline.ast-helpers :refer :all]
              [sf.substreams.v1 :as sf]
              [clojure.string :as string]
              [spyglass.streamline.alpha.ast :as ast]
              [clojure.string :as str]))

;; (defn slurp-bytes
;;   "Slurp the bytes from a slurpable thing"
;;   [x]
;;   (with-open [in (clojure.java.io/input-stream x)
;;               out (java.io.ByteArrayOutputStream.)]
;;     (clojure.java.io/copy in out)
;;     (.toByteArray out)))

;; (defn slurp-spkg
;;   "Slurps an spkg file into a sf/Package message"
;;     [file]
;;   (sf/new-Package (slurp-bytes file)))
                 
(defn clojure-type-to-protobuf-type [value]
  (cond
    (string? value) "string"
    (integer? value) "int32"
    (float? value) "float"
    (map? value) "message"
    :else (throw (IllegalArgumentException. (str "Unsupported type: " (type value))))))

(defn address?
 [input]
 (= input "address"))

(defn map-entry-to-protobuf-field [index key value]
  (str (clojure-type-to-protobuf-type value) " " key " = " index ";"))

(defn keyword->str
 [keyword]
 (string/replace (str keyword) ":" ""))

(defn map-to-protobuf [map]
   (apply str (for [[index [k v]] (map-indexed vector map)]
               (map-entry-to-protobuf-field index k v))))

(defmulti ->protobuf first)

(def test-struct [:struct-def "Zap" [:struct-field "user" [:solidity-type "address"]] [:struct-field "balance" [:solidity-type "uint256"]]])

(defn solidity-type->protobuf-type
  [value]
  (cond 
    ()))
                  
(defmethod ->protobuf :struct-def 
  [input]
  (let [[_ struct-name & fields] input
        fields (map-indexed vector fields)]
   [struct-name fields]))

(->protobuf test-struct)


(let [example-map {:name "ChatGPT" :age 4}]
  (println (map-to-protobuf example-map)))
