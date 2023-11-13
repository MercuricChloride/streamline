(ns streamline.ast.writer
  (:require
   [clojure.java.io :as io]
   [protojure.protobuf :as protojure]
   [streamline.ast.file-constructor :refer [construct-streamline-file]]))

(defn write-file [input path]
  (with-open [o (io/output-stream path)]
    (.write o input)))

(defn write-ast
  [ast path]
  (let [file (protojure/->pb (construct-streamline-file ast))]
    (write-file file path)))
