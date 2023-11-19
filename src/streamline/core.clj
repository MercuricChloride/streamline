(ns streamline.core
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as string]
   [pogonos.core :as pg]
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
                                             array-symbol (str event-symbol "Array")
                                             event-message (str namespace "." event-symbol)
                                             array-message (str namespace "." event-symbol "Array")]
                                         [{event-symbol event-message} {array-symbol array-message}])))
                                (concat [{(str name ".Events") (str namespace "." name ".Events")}]))) contracts))))

(defn structs->symbols
  "Converts a list of structs into a map from their event symbols to their protobuf message type"
  [structs namespace]
  (into {} (flatten (map (fn [{:keys [:name]}]
                           (let [struct-path (str namespace "." name)
                                 array-name (str name "Array")
                                 array-path (str struct-path "Array")]
                             [{name struct-path} {array-name array-path}])) structs))))
(defn render-module
  [module]
  (pg/render-resource "templates/rust/functions/map_module.mustache" (into {} module)))

(defn format-module
  [resolved-dag module]
  (let [{:keys [:identifier]} module
        io (get resolved-dag identifier)
        inputs (->> io
                    :inputs
                    (map #(or (:output (get resolved-dag %)) %))
                    (map #(string/replace % "." "::"))
                    (map-indexed (fn [index val] (str "input" (inc index) ": " val)))
                    (string/join ", "))
        output (->> io
                    :output
                    (#(string/replace % "." "::")))
        protobuf-signature {:inputs inputs :output output}]
    (assoc module :signature protobuf-signature)))

(defn protobuf-node?
  "Returns if an ast node will be used to generate a protobuf message"
  [[kind & _]]
  (get #{:interface-def
         :struct-def
         :event-def} kind))

(defmulti add-namespace
  "Adds a namespace and name field to the meta of the nodes that need it to the parse tree"
  (fn [node _namespace] (first node)))

(defmethod add-namespace :default
  [node namespace]
  (if (protobuf-node? node)
    (let [m (meta node)
          name (->> node
                    second
                    csk/->PascalCase)
          new-meta (assoc m
                          :namespace namespace
                          :name name)]
      (with-meta node new-meta))
    node))

(defmethod add-namespace :interface-def
  [node namespace]
  (let [m (meta node)
        interface-name (->> node
                            second
                            csk/->PascalCase)
        new-meta (assoc m
                        :namespace namespace
                        :name interface-name)
        [kind name & children] node
        children-namespace (str namespace "." name)
        new-children (map #(add-namespace % children-namespace) children)
        node (concat [kind name] new-children)]
    (with-meta node new-meta)))

(defn get-namespace
  "Returns the namespace for a streamline file"
  [parse-tree]
  (let [[_type _kind namespace] (first parse-tree)]
    (csk/->snake_case namespace)))

(defn add-namespaces
  "Adds a namespace to the meta of the nodes that need it to the parse tree"
  [parse-tree]
  (let [namespace (get-namespace parse-tree)]
    (map #(add-namespace % namespace) parse-tree)))

(defn build-proto-message
  [name fields]
  (pg/render-resource "templates/proto/messages.mustache" {:name name
                                                           :fields fields}))

(defmulti ->message
  "Converts a node into a protobuf message"
  (fn [node _symbol-table] (first node)))

(defmethod ->message :default
  [node st]
  (if (protobuf-node? node)
    (let [{:keys [:namespace :name]} (meta node)
          fields "//todo;"]
      (build-proto-message name fields))
    nil))

(defmethod ->message :interface-def
  [node st]
  (let [{:keys [:namespace :name]} (meta node)
        [_ & children] node
        fields (string/join "\n" (map ->message children))]
    (build-proto-message name fields)))

(defmulti store-symbol
  "Stores the symbols for a node in a symbol table"
  (fn [node _symbol-table] (first node)))

(defmethod store-symbol :default
  [node st]
  (if (protobuf-node? node)
    (let [{:keys [:namespace :name]} (meta node)
          symbol (str namespace "." name)
          array-name (str name "[]")
          array-symbol (str namespace "." name "Array")]
      (assoc st name symbol array-name array-symbol))
    st))

(defn resolve-symbol
  [symbol symbol-table]
  (loop [parts (string/split symbol #"\.")
         symbol-table symbol-table]
    (let [resolved-symbol (get symbol-table (first parts))]
      (if (= (count parts) 1)
      resolved-symbol
      (recur (rest parts) resolved-symbol)))))

(defmethod store-symbol :interface-def
  [node st]
  (let [{:keys [:name]} (meta node)
        [_ & children] node
        sub-table (reduce (fn [acc child]
                            (store-symbol child acc))
                          {}
                          children)]
    (assoc st name sub-table)))

(defn store-symbols
  "Stores the symbols for a parse tree in a symbol table"
  [parse-tree]
  (reduce (fn [acc node]
            (store-symbol node acc))
          {}
          parse-tree))

(defn create-protobuf-defs
  "Creates the protobuf file for a streamline file"
  [parse-tree]
  (let [namespace (get-namespace parse-tree)]
    {:namespace namespace
     :messages (string/join "\n" (map ->message parse-tree))}))

(let [parse-tree (parser (slurp "sushi.strm"))
      w-namespaces (add-namespaces parse-tree)
      symbol-table (store-symbols w-namespaces)
      test-symbol "SomethingElse.Transfer[]"
      ;(string/split "Sushi.PoolCreated[]" #"\.")
      ;; protodefs (->> w-namespaces
      ;;                create-protobuf-defs
      ;;                (pg/render-resource "templates/proto/protofile.mustache"))
      ]
  (resolve-symbol test-symbol symbol-table))

;; (let [base-ast (construct-base-ast sushi)
;;       contracts (:contracts base-ast)
;;       structs (:types base-ast)

;;       namespace (:name (:meta base-ast))

;;       protobuf-symbol-table (merge
;;                              {"Block" "sf.ethereum.type.v2.Block"}
;;                              (contracts->symbols contracts namespace)
;;                              (structs->symbols structs namespace))

;;       protobuf-paths (concat
;;                       [namespace]
;;                       (map #(str namespace "." (:name %)) contracts))
;;       resolved-dag (into {} (map (fn [node]
;;                                    (let [inputs (:inputs node)
;;                                          output (:output node)
;;                                          name (:module node)
;;                                          inputs (map #(or (get protobuf-symbol-table %) %) inputs)
;;                                          output (or (get protobuf-symbol-table output) output)]
;;                                      [name {:inputs inputs :output output}])) (construct-dag base-ast)))]
;;   (->> base-ast
;;        :modules
;;        (map #(format-module resolved-dag %))
;;        (map render-module)
;;        (string/join "\n\n")))
