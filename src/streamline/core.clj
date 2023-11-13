(ns streamline.core
  (:require
   [clojure.data.json :as json]
   [spyglass.streamline.alpha.ast :as ast]
   [streamline.ast.analysis.type-validation :refer [construct-symbol-table
                                                    get-array-types]]
   [streamline.ast.new-parser :refer [->node]]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [write-ast]]
   [streamline.protobuf.helpers :refer [contract->protobuf structs->protobuf]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast path)))

(def ast (parser (slurp "streamline.strm")))

(defmulti store-node (fn [node acc]
                       (class node)))

(defmethod store-node :default
  [_ acc]
  acc)

(defmethod store-node spyglass.streamline.alpha.ast.FileMeta-record
  [node acc]
  (assoc acc :meta node))

(defmethod store-node spyglass.streamline.alpha.ast.StructDef-record
  [node acc]
  (assoc acc :types (conj (:types acc) node)))

(defmethod store-node spyglass.streamline.alpha.ast.ModuleDef-record
  [node acc]
  (assoc acc :modules (conj (:modules acc) node)))

(defmethod store-node spyglass.streamline.alpha.ast.Conversion-record
  [node acc]
  (assoc acc :conversions (conj (:conversions acc) node)))

(defmethod store-node spyglass.streamline.alpha.ast.ContractAbi-record
  [node acc]
  (let [{:keys [:events :functions]} node
        abi-json (->> (concat events functions)
                      (into [])
                      (json/write-str))
        node (conj node {:abi-json abi-json})]
    (assoc acc :contracts (conj (:contracts acc) node))))

(defmethod store-node spyglass.streamline.alpha.ast.ContractInstance-record
  [node acc]
  (let [{:keys [:address :contract-interface :instance-name]} node]
    (assoc acc :instances (conj (:instances acc) node))))

(defn- construct-base-ast
  [parse-tree]
  (->> (loop [parse-tree parse-tree
              acc {}]
         (if (empty? parse-tree)
           acc
           (let [node (->node (first parse-tree))
                 remaining (rest parse-tree)]
             (recur remaining (store-node node acc)))))
       (ast/new-StreamlineFile)))

(defn- construct-protobuf-files
  [base-ast symbol-table]
  (let [{:keys [:modules :contracts :meta :types]} base-ast
        file-namespace (:name meta)
        array-types (get-array-types modules symbol-table)
        contract-protobufs (into [] (map #(contract->protobuf % array-types file-namespace) contracts))
        struct-protobufs (structs->protobuf types array-types file-namespace)
        protobufs (conj contract-protobufs struct-protobufs)]
    (assoc base-ast
           :protobufs protobufs
           :array-types array-types)))

(defn construct-streamline-file
  "Constructs a StreamlineFile protobuf. Note that this function is done in steps.
  First, we parse the literal syntax nodes into a 'Base' Ast.
  Then, we use that base AST to construct some more complex things such as the symbol table.
  Finally, we use the data there to construct all of the protobuf definitions and module definitions.

  I could do this all in one step, but I think it's easier to understand this way."
  [parse-tree]
  (let [base-ast (construct-base-ast parse-tree)
        {:keys [:contracts :types :instances]} base-ast
        symbol-table (construct-symbol-table contracts types instances)
        with-protobufs (construct-protobuf-files base-ast symbol-table)]
    with-protobufs))

(construct-streamline-file ast)



;; (def sushi (parser (slurp "sushi.strm")))

;;(write-ast sushi "sushi.cstrm")
;; (write-ast ast "streamline-test.cstrm")
;; (def astproto (ast->file sushi))
