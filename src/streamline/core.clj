(ns streamline.core
  (:require
   [streamline.ast.file-constructor :refer [construct-base-ast]]
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
(write-ast sushi "sushi.cstrm")

(defmulti resolve-protobuf-types (fn [node symbol-table namespace] (class node)))


(defn symbol-resolver
  "Takes in a list of types and a namespace, and returns the protobuf type for each symbol present in the AST"
  ([type-list namespace]
   (symbol-resolver type-list "" namespace {}))

  ([type-list namespace parent-namespace symbol-table]
   (loop [type-list type-list
          namespace namespace
          symbol-table symbol-table]
     (if (empty? type-list)
       symbol-table
       (let [type (first type-list)
             remaining (rest type-list)
             type-name (if parent-namespace
                         (str parent-namespace "." (:name type))
                         (:name type))
             symbol (str namespace "." type-name)]
         (recur remaining namespace (assoc symbol-table type-name symbol)))))))

(defmethod resolve-protobuf-types spyglass.streamline.alpha.ast.ContractAbi-record
  [contract-abi symbol-table namespace]
  (let [{:keys [:name :events]} contract-abi]
    (symbol-resolver events namespace name {})))

(defmethod resolve-protobuf-types spyglass.streamline.alpha.ast.StructDef-record
  [struct-def symbol-table namespace]
  (let [name (:name struct-def)]
    (symbol-resolver struct-def namespace name {})))

(defmethod resolve-protobuf-types spyglass.streamline.alpha.ast.StreamlineFile-record
  [streamline-file _ _]
  (let [{:keys [:types :contracts :meta]} streamline-file
        namespace (:name meta)
        symbol-table {}]
    (conj (symbol-resolver types namespace nil symbol-table)
          (->> contracts
            (map #(resolve-protobuf-types % symbol-table namespace))
            (map conj)))))

(let [base-ast (construct-base-ast sushi)
      test-contract (first (:contracts base-ast))
      namespace (:name (:meta base-ast))
      types (:types base-ast)]
  (resolve-protobuf-types base-ast nil nil))

;; (write-ast ast "streamline-test.cstrm")
;; (def astproto (ast->file sushi))
