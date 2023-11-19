(ns streamline.ast.metadata
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as string]
   [streamline.ast.helpers :refer [find-child format-type]]))

(defn protobuf-node?
  "Returns if an ast node will be used to generate a protobuf message"
  [[kind & _]]
  (get #{:interface-def
         :struct-def
         :event-def} kind))

;;;========================================
;;; NAMESPACE HELPERS
;;; =======================================
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

;;;========================================
;;; SYMBOL TABLE HELPERS
;;; =======================================

(defn solidity-type?
  [type]
  (or (string/starts-with? type "uint")
      (string/starts-with? type "int")
      (string/starts-with? type "bytes")
      (string/starts-with? type "bool")
      (string/starts-with? type "string")
      (string/starts-with? type "address")))

(defn push-metadata
  "Pushes metadata to a node"
  [node metadata-map]
  (let [m (meta node)
        new-meta (merge m metadata-map)]
    (with-meta node new-meta)))

(defn solidity->protobuf-type
  [type]
  (cond (= "bool" type) "boolean"
        (= "string" type) "string"
        :else "string"))

(defn lookup-symbol
  [symbol symbol-table]
  (if (solidity-type? symbol)
    (solidity->protobuf-type symbol)
    (loop [parts (string/split symbol #"\.")
           symbol-table symbol-table]
      (let [resolved-symbol (get symbol-table (first parts))]
        (if (= (count parts) 1)
          resolved-symbol
          (recur (rest parts) resolved-symbol))))))

(defn get-module-output-type
  "Returns the output type of a module"
  ([module]
   (-> module
       (find-child :module-signature)
       (find-child :module-output)
       last
       (format-type)))
  ([module symbol-table]
   (-> module
       (find-child :module-signature)
       (find-child :module-output)
       last
       (format-type)
       (lookup-symbol symbol-table))))

(defmulti store-symbol
  "Stores the symbols for a node in a symbol table"
  (fn [node _symbol-table] (first node)))

(defn store-symbols
  "Stores the symbols for a parse tree in a symbol table"
  [parse-tree]
  (reduce (fn [acc node]
            (store-symbol node acc))
          {}
          parse-tree))

(defmethod store-symbol :default
  [node st]
  (if (protobuf-node? node)
    (let [{:keys [:namespace :name]} (meta node)
          symbol (str namespace "." name)
          array-name (str name "[]")
          array-symbol (str namespace "." name "Array")]
      (assoc st name symbol array-name array-symbol))
    st))

(defmethod store-symbol :interface-def
  [node st]
  (let [{:keys [:name]} (meta node)
        [_ & children] node
        sub-table (reduce (fn [acc child]
                            (store-symbol child acc))
                          {}
                          children)]
    (assoc st name sub-table)))

(defn store-module-output
  [node st]
  (if (= (first node) :module)
    (let [[_ _ module-name] node
          signature-output (get-module-output-type node st)
          node (push-metadata node {:output-type signature-output})]
      [node (assoc st module-name signature-output)])
    [node st]))

(defn store-module-outputs
  [parse-tree st]
  (reduce (fn [[tree-acc st-acc] node]
            (let [[new-node new-st] (store-module-output node st-acc)]
              [(conj tree-acc new-node) (merge st-acc new-st)]))
          [[] st]
          parse-tree))

;;;========================================
;;; SYMBOL RESOLVER FOR AST
;;; =======================================

(defmulti resolve-type
  "Resolves the type of a node to its protobuf type"
  (fn [node _symbol-table] (first node)))

(defmethod resolve-type :type
  [node symbol-table]
  (let [type (format-type node)
        resolved-type (lookup-symbol type symbol-table)]
    (push-metadata node {:type resolved-type})))

(defmethod resolve-type :struct-def
  [node symbol-table]
  (let [m (meta node)
        [kind name & children] node
        new-node (->> children
                      (map #(resolve-type % symbol-table))
                      (concat [kind name]))]
    (with-meta new-node m)))

;; NOTE This method should only be called after we add the module outputs to the symbol table
(defmethod resolve-type :module
  [node symbol-table]
  (let [inputs (as-> node n
                      (find-child n :module-signature)
                      (find-child n :module-inputs)
                      (map #(format-type %) (rest n))
                      (map (fn [input]
                             (let [input-symbol (lookup-symbol input symbol-table)
                                   input-kind "map"
                                   input-name input]
                               {:type input-symbol
                                :kind input-kind
                                :name input-name})) n))]
    (push-metadata node {:inputs inputs})))

(defmethod resolve-type :interface-def
  [node symbol-table]
  (let [m (meta node)
        [kind name & children] node
        new-node (->> children
                      (map #(resolve-type % symbol-table))
                      (concat [kind name]))]
    (with-meta new-node m)))

(defmethod resolve-type :event-def
  [node symbol-table]
  (let [m (meta node)
        [kind name & children] node
        new-node (->> children
                      (map #(resolve-type % symbol-table))
                      (concat [kind name]))]
    (with-meta new-node m)))

(defmethod resolve-type :indexed-event-param
  [node symbol-table]
  (let [[kind type name] node
        resolved-type (resolve-type type symbol-table)
        proto (-> resolved-type
                  (meta)
                  :type)
        new-meta {:type proto
                  :name name
                  :indexed true
                  :repeated (string/ends-with? type "[]")}
        new-node (concat [kind resolved-type name])]
    (push-metadata new-node new-meta)))

(defmethod resolve-type :non-indexed-event-param
  [node symbol-table]
  (let [[kind type name] node
        resolved-type (resolve-type type symbol-table)
        proto (-> resolved-type
                  (meta)
                  :type)
        new-meta {:type proto
                  :name name
                  :indexed false
                  :repeated (string/ends-with? type "[]")}
        new-node (concat [kind resolved-type name])]
    (push-metadata new-node new-meta)))

(defmethod resolve-type :struct-field
  [node symbol-table]
  (let [[kind type name] node
        proto (-> type
                  (resolve-type symbol-table)
                  (meta)
                  :type)
        new-node (concat [kind name type])
        new-meta {:type proto
                  :name name
                  :repeated (string/ends-with? type "[]")}]
    (push-metadata new-node new-meta)))

(defmethod resolve-type :default
  [node _]
  (if (string? node)
    node
    (let [meta (meta node)]
      (with-meta node meta))))

;;;========================================
;;; PUBLIC METADATA HELPERS
;;; =======================================
(defn add-metadata
  "Adds all of the metadata to the parse tree"
  [parse-tree]
  (let [parse-tree (add-namespaces parse-tree)
        symbol-table (store-symbols parse-tree)
        [parse-tree symbol-table] (store-module-outputs parse-tree symbol-table)
        parse-tree (map #(resolve-type % symbol-table) parse-tree)]
    [parse-tree symbol-table]))
