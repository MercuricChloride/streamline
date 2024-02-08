(ns streamline.lang.metadata
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.pprint :as pprint]
   [clojure.string :as string]
   [streamline.ast.helpers :refer [find-child format-type]]
   [streamline.templating.helpers :refer [->snake-case lookup-symbol]]))

(add-tap pprint/pprint)

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
    (->snake-case namespace)))

(defn add-namespaces
  "Adds a namespace to the meta of the nodes that need it to the parse tree"
  [parse-tree]
  (let [namespace (get-namespace parse-tree)]
    (map #(add-namespace % namespace) parse-tree)))

;;;========================================
;;; SYMBOL TABLE HELPERS
;;; =======================================

(defn push-metadata
  "Pushes metadata to a node"
  [node metadata-map]
  (let [m (meta node)
        new-meta (merge m metadata-map)]
    (with-meta node new-meta)))

(defn get-module-output-type
  "Returns the output type of a module"
  ([module]
   (-> module
       (find-child :module-signature)
       (find-child :module-output)
       last
       (format-type)))
  ([module symbol-table]
   (tap> (-> module
             (find-child :module-signature)))
   (-> module
       (find-child :module-signature)
       (find-child :module-output)
       (format-type)
       (lookup-symbol symbol-table :raw-type true))))

(defmulti store-symbol
  "Stores the symbols for a node in a symbol table"
  (fn [node & args] (first node)))

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

(defmethod store-symbol :event-def
  [node sub-table]
  (let [{:keys [:namespace :name]} (meta node)
        symbol (str namespace "." name)
        array-name (str name "[]")
        array-symbol (str namespace "." name "Array")
        interface-name (last (string/split namespace #"\."))
        event-module (str "map_" (->snake-case interface-name) "_" (->snake-case name))
        sub-table (assoc sub-table name symbol array-name array-symbol)
        parent-name (last (string/split namespace #"\."))]
    [sub-table {:event-fn event-module
                :event-fn-output (str parent-name "." array-name)
                :event-fn-event (str parent-name "." name)}]))

(defmethod store-symbol :interface-def
  [node st]
  (let [{:keys [:name]} (meta node)
        [_ & children] node
        [sub-table event-fns] (reduce (fn [[st-acc e-acc] child]
                                        (let [output (store-symbol child st-acc)]
                                          (if (not= (count output) 0)
                                            [(first output) (conj e-acc (second output))]
                                            [output e-acc])))
                                      [{} []]
                                      children)
        symbol-table (reduce (fn [st-acc {:keys [:event-fn :event-fn-output]}]
                               (assoc st-acc event-fn {:output event-fn-output
                                                       :module-kind "mfn"}))
                             (assoc st name sub-table)
                             event-fns)
        event-fns (reduce (fn [acc {:keys [:event-fn :event-fn-output]}]
                            (assoc acc event-fn-output event-fn)) ;store from output->name so we can lookup by output
                          {}
                          event-fns)
        st-meta-fns (:event-fns (meta st))]
    (push-metadata symbol-table {:event-fns (merge event-fns st-meta-fns)})))

(defn store-module-output
  [node st]
  (if (= (first node) :module)
    (let [[_ kind module-name] node
          _ (tap> node)
          signature-output (get-module-output-type node st)
          node (push-metadata node {:output-type signature-output})
          node (push-metadata node {:name module-name})
          node (push-metadata node {:module-kind kind})]
      [node (assoc st module-name {:output signature-output
                                   :module-kind kind})])
    [node st]))

(defn store-module-outputs
  [parse-tree st]
  (reduce (fn [[tree-acc st-acc] node]
            (let [[new-node new-st] (store-module-output node st-acc)]
              [(conj tree-acc new-node) (merge st-acc new-st)]))
          [[] st]
          parse-tree))

;;;========================================
;;; PUBLIC METADATA HELPERS
;;; =======================================
(defn get-symbol-table
  "Returns the symbol table for the parse tree"
  [parse-tree]
        ; create the initial parse tree
  (let [parse-tree (add-namespaces parse-tree)
        ; create the initial symbol-table
        symbol-table (store-symbols parse-tree)
        ; update the symbol table and parse tree with the module output metadata
        [parse-tree symbol-table] (store-module-outputs parse-tree symbol-table)]
    symbol-table))
