(ns streamline.core
  (:require
   [clojure.inspector :refer [inspect-tree]]
   [streamline.ast.helpers :refer [generate-abi]]
   [streamline.ast.metadata :as metadata]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [write-ast]])
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
      [ast symbol-table] (metadata/add-metadata parse-tree)
      abi-json (generate-abi ast)]
  symbol-table)
