(ns streamline.core
  (:require
   [clojure.string :as string]
   [streamline.ast.analysis.type-validation :refer [symbol-table]]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [ast->file write-ast]]
   [streamline.ast.new-parser :refer [->node]]
   [streamline.protobuf.helpers :refer [array-type->protobuf]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast path)))

(def ast (parser (slurp "streamline.strm")))

(->> ast
     (map ->node)
     (map class))

(defmulti store-node (fn [node acc]
                       (class node)))

(defmethod store-node :default
  [_ acc]
  acc)

(defmethod store-node spyglass.streamline.alpha.ast.FileMeta-record
  [node acc]
  (assoc acc :file-meta node))

(defmethod store-node spyglass.streamline.alpha.ast.StructDef-record
  [node acc]
  (assoc acc :types (conj (:types acc) node)))

(defmethod store-node spyglass.streamline.alpha.ast.ModuleDef-record
  [node acc]
  (assoc acc :module (conj (:module acc) node)))

(defn format-ast
  [parse-tree]
  (loop [parse-tree parse-tree
         acc {}]
    (if (empty? parse-tree)
      acc
      (let [node (->node (first parse-tree))
            remaining (rest parse-tree)]
        (recur remaining (store-node node acc))))))

(format-ast ast)

(def sushi (parser (slurp "sushi.strm")))

(write-ast sushi "sushi.cstrm")
(write-ast ast "streamline-test.cstrm")
(def astproto (ast->file sushi))
