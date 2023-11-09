(ns streamline.core
  (:require
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [write-ast]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast)))


(def ast (parser (slurp "streamline-test.strm")))
