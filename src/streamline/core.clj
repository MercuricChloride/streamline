(ns streamline.core
  (:require
   [clojure.string :as string]
   [streamline.ast.analysis.type-validation :refer [symbol-table]]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [ast->file write-ast]]
   [streamline.protobuf.helpers :refer [array-type->protobuf]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast path)))

(def ast (parser (slurp "streamline.strm")))
(def sushi (parser (slurp "sushi.strm")))
(write-ast sushi "sushi.cstrm")
(write-ast ast "streamline-test.cstrm")
(def astproto (ast->file sushi))
