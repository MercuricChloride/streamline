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
    (write-ast ast)))

(def ast (parser (slurp "streamline.strm")))
(def astproto (ast->file ast))

;; (let [interfaces (:contracts astproto)
;;       struct-defs (:types astproto)
;;       contract-instances (:instances astproto)
;;       table (symbol-table interfaces struct-defs contract-instances)]
;;   (map array-type->protobuf (get-array-types (:modules astproto) table)))
