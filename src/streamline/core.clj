(ns streamline.core
  (:require
   [protojure.protobuf :as protojure]
   [streamline.ast.file-constructor :refer [construct-streamline-file]]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [write-ast]]
   [spyglass.streamline.alpha.ast :as ast])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast path)))

(def parse-tree (parser (slurp "streamline.strm")))

(def sushi (parser (slurp "sushi.strm")))
(construct-streamline-file sushi)
(write-ast sushi "sushi.cstrm")



;; (write-ast ast "streamline-test.cstrm")
;; (def astproto (ast->file sushi))
