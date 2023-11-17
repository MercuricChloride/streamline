(ns streamline.core
  (:require
   [streamline.ast.dag :refer [construct-dag]]
   [streamline.ast.file-constructor :refer [construct-base-ast]]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [write-ast]]
   [pogonos.core :as pg])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast path)))

(def parse-tree (parser (slurp "streamline.strm")))

(def sushi (parser (slurp "sushi.strm")))
;(write-ast sushi "sushi.cstrm")

;; (defn symbol-resolver
;;   "Takes in a list of types and a namespace, and returns the protobuf type for each symbol present in the AST"
;;   ([type-list namespace]
;;    (symbol-resolver type-list "" namespace {}))

;;   ([type-list namespace parent-namespace symbol-table]
;;    (loop [type-list type-list
;;           namespace namespace
;;           symbol-table symbol-table]
;;      (if (empty? type-list)
;;        symbol-table
;;        (let [type (first type-list)
;;              remaining (rest type-list)
;;              type-name (if parent-namespace
;;                          (str parent-namespace "." (:name type))
;;                          (:name type))
;;              symbol (str namespace "." type-name)]
;;          (recur remaining namespace (assoc symbol-table type-name symbol)))))))

;; (defmulti resolve-protobuf-types (fn [node file-namespace] (class node)))

;; (defmethod resolve-protobuf-types spyglass.streamline.alpha.ast.ContractAbi-record
;;   [contract namespace]
;;   (let [{:keys [:events :name]} contract]
;;     (reduce (fn [acc event]
;;               (let [event-name (:name event)]
;;                 (conj acc {(str name "." event-name) (str namespace "." name "." event-name)})))
;;             {}
;;             events)))

;; (defmethod resolve-protobuf-types spyglass.streamline.alpha.ast.StructDef-record
;;   [struct-def namespace]
;;   (let [{:keys [:name :fields]} struct-def]
;;     {name (str namespace "." name)}))

;; (defmethod resolve-protobuf-types spyglass.streamline.alpha.ast.ModuleDef-record
;;   [module-def namespace]
;;   (let [{:keys [:identifier :signature]} module-def
;;         {:keys [:inputs :output]} signature
;;         output (string/replace output "[]" "Array")]
;;     {identifier output}))

;; (defn resolve-ast-module-protobuf-paths
;;   [modules protobuf-paths]
;;   (let [module-map (reduce (fn [acc module]
;;                              (let [{:keys [:name :signature]} module
;;                                    {:keys [:inputs :output]} signature
;;                                    inputs (into [] (map #(string/replace % "[]" "Array") inputs))
;;                                    output (string/replace output "[]" "Array")
;;                                    protobuf-inputs (map #(get protobuf-paths %) inputs)
;;                                    protobuf-output (get protobuf-paths output)
;;                                    protobuf-signature {:inputs protobuf-inputs :output protobuf-output}]
;;                                (assoc acc name protobuf-signature)))
;;                            {}
;;                            modules)]
;;     module-map))

;; (defn resolve-ast-protobuf-paths
;;   [ast]
;;   (let [{:keys [:meta :contracts :types :imports :modules]} ast
;;         file-namespace (:name meta)
;;         paths (reduce (fn [acc contract]
;;                         (conj acc (resolve-protobuf-types contract file-namespace)))
;;                       {}
;;                       contracts)
;;         paths (reduce (fn [acc type]
;;                         (conj acc (resolve-protobuf-types type file-namespace)))
;;                       paths types)
;;         paths (reduce (fn [acc import]
;;                         (conj acc (resolve-protobuf-types import file-namespace)))
;;                       paths imports)
;;         paths (reduce (fn [acc module]
;;                         (conj acc (resolve-protobuf-types module file-namespace)))
;;                       paths modules)]
;;     paths))

;; (defmethod resolve-protobuf-types streamline.ast.new_parser.ast-import-statement
;;   [import namespace]
;;   (let [{:keys [:import-path :rename]} import
;;         ast (parser (slurp import-path))
;;         base-ast (construct-base-ast ast)
;;         resolved-symbols (resolve-ast-protobuf-paths base-ast)]
;;     (if rename
;;       (->> (map (fn [[k v]]
;;                   (if (string/starts-with? v (:name (:meta base-ast)))
;;                     [(str rename "." k) v]
;;                     [(str rename "." k) (str rename "." v)]))
;;                 resolved-symbols)
;;            (into {}))
;;       resolved-symbols)))

;; (defn contract->protobuf
;;   "Converts a Contract AST node into a protobuf file"
;;   [input array-types file-namespace]
;;   (let [name (:name input)
;;         array-types (->> array-types
;;                          (filter #(string/starts-with? % name))
;;                          (map array-type->protobuf)
;;                          (string/join "\n\n"))
;;         events (events->protobuf (:events input))
;;         protobufs (str events "\n\n" array-types)
;;         functions (:functions input)
;;         template (slurp "./templates/protobuf.txt")]
;;     (-> template
;;         (string/replace "$$PACKAGE-NAME$$" (str file-namespace "." name))
;;         (string/replace "$$TYPES$$" protobufs))))

(defn contracts->symbols
  "Converts a list of contracts into a map from their event symbols to their protobuf message type"
  [contracts namespace]
  (into {} (flatten (map (fn [{:keys [:name :events]}]
                           (->> events
                                (map (fn [event]
                                       (let [event-name (:name event)
                                             event-symbol (str name "." event-name)
                                             array-symbol (str event-symbol "[]")
                                             event-message (str namespace "." event-symbol)
                                             array-message (str namespace "." event-symbol "Array")]
                                         [{event-symbol event-message} {array-symbol array-message}])))
                                (concat [{(str name ".Events") (str namespace "." name ".Events")}]))) contracts))))

(defn structs->symbols
  "Converts a list of structs into a map from their event symbols to their protobuf message type"
  [structs namespace]
  (into {} (flatten (map (fn [{:keys [:name]}]
                           (let [struct-path (str namespace "." name)
                                 array-name (str name "[]")
                                 array-path (str struct-path "Array")]
                             [{name struct-path} {array-name array-path}])) structs))))

(let [base-ast (construct-base-ast sushi)
      contracts (:contracts base-ast)
      structs (:types base-ast)

      namespace (:name (:meta base-ast))

      protobuf-symbol-table (merge
                             {"Block" "sf.ethereum.type.v2.Block"}
                             (contracts->symbols contracts namespace)
                             (structs->symbols structs namespace))

      protobuf-paths (concat
                      [namespace]
                      (map #(str namespace "." (:name %)) contracts))
      resolved-dag (map (fn [node]
                          (let [inputs (:inputs node)
                                output (:output node)
                                inputs (map #(or (get protobuf-symbol-table %) %) inputs)
                                output (or (get protobuf-symbol-table output) output)]
                            {:inputs inputs :output output})) (construct-dag base-ast))]
  resolved-dag)

(pg/render-file "templates/rust/functions/map_module.mustache" {:name "epic_function"})
