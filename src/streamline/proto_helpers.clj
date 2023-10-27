(ns streamline.proto-helpers
    (:require [streamline.ast-helpers :refer :all]
              [clojure.java.io]
              [sf.substreams.v1 :as sf]
              [spyglass.streamline.alpha.ast :as ast]))

(defn slurp-bytes
  "Slurp the bytes from a slurpable thing"
  [x]
  (with-open [in (clojure.java.io/input-stream x)
              out (java.io.ByteArrayOutputStream.)]
    (clojure.java.io/copy in out)
    (.toByteArray out)))

(defn slurp-spkg
  "Slurps an spkg file into a sf/Package message"
    [file]
  (sf/new-Package (slurp-bytes file)))


(defn lambda->proto
  "Converts a lambda expression into a protobuf message"
  [lambda]
  (let [inputs (:inputs lambda)
        body (:expression lambda)]
    (ast/new-Lambda {:inputs inputs
                 ; TODO Add the expression
                 })))
