(ns streamline.core
  (:require
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [write-ast]]
   [streamline.ast.file-constructor :refer [construct-streamline-file]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast path)))

(def parse-tree (parser (slurp "streamline.strm")))

(construct-streamline-file parse-tree)



;; (def sushi (parser (slurp "sushi.strm")))
;;(write-ast sushi "sushi.cstrm")
;; (write-ast ast "streamline-test.cstrm")
;; (def astproto (ast->file sushi))
