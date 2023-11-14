(ns streamline.ast.file-constructor
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string]
   [spyglass.streamline.alpha.ast :as ast]
   [streamline.ast.analysis.type-validation :refer [construct-symbol-table
                                                    get-array-types]]
   [streamline.ast.new-parser :refer [->node]]
   [streamline.protobuf.helpers :refer [contract->protobuf structs->protobuf]]))

(defmulti store-node
  "This method is responsible for storing the node in the StreamlineFile protobuf."
  (fn [node acc]
    (class node)))

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
  (assoc acc :instances (conj (:instances acc) node)))

(defmethod store-node streamline.ast.new_parser.ast-import-statement
  [node acc]
  acc)
    ;(assoc acc :imports (conj (:imports acc) node)))

(defn- format-module-signatures
  "Replaces [] with Array for module signatures"
  [module-defs]
  (map (fn [module-def]
         (let [signature (:signature module-def)
               {:keys [:inputs :output]} signature
               inputs (into [] (map #(string/replace % "[]" "Array") inputs))
               output (string/replace output "[]" "Array")]
           (assoc module-def :signature {:inputs inputs :output output}))) module-defs))

(defn construct-base-ast
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
        protobufs (conj contract-protobufs struct-protobufs)
        modules (format-module-signatures modules)]
    (assoc base-ast
           :protobufs protobufs
           :array-types array-types
           :modules modules)))

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
    (ast/new-StreamlineFile with-protobufs)))
