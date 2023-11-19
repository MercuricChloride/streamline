(ns streamline.core
  (:require
   [streamline.ast.metadata :as metadata]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [write-ast]]
   [streamline.templating.protobuf :refer [create-protobuf-defs]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast path)))

(def parse-tree (parser (slurp "streamline.strm")))

(def sushi (parser (slurp "sushi.strm")))

(let [parse-tree (parser (slurp "sushi.strm"))
      [ast symbol-table] (metadata/add-metadata parse-tree)]
  (create-protobuf-defs ast))
