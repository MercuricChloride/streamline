(ns streamline.core
  (:require
   [streamline.ast.helpers :refer [generate-abi]]
   [streamline.ast.metadata :as metadata :refer [get-namespace]]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [write-ast]]
   [streamline.templating.rust.helpers :refer [get-all-conversions]]
   [streamline.templating.yaml.helpers :refer [generate-yaml]])
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
      abi-json (generate-abi ast)
      ast-ns (get-namespace ast)
      modules (->> ast
                   (filter #(= (first %) :module)))
      interfaces (->> ast
                      (filter #(= (first %) :interface-def)))
      yaml (generate-yaml ast-ns modules interfaces symbol-table)]
  (get-all-conversions ast))
