(ns streamline.core
  (:require
   [clojure.string :as string]
   [streamline.ast.analysis.type-validation :refer [instances->types
                                                    symbol-table]]
   [streamline.ast.parser :refer [parser]]
   [streamline.ast.writer :refer [ast->file write-ast]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)
        ast (parser (slurp path))]
    (write-ast ast)))

(def ast (parser (slurp "streamline.strm")))
(def astproto (ast->file ast))

(defn type-kind
  [type]
  (cond
    (and (string/includes? type ".") (string/ends-with? type "[]")) :fully-qualified-array
    (string/includes? type ".") :fully-qualified
    (string/ends-with? type "[]") :array
    :else :primitive))

(defmulti lookup-type
  "Returns the type of a variable"
  (fn [type symbol-table] (type-kind type)))

(defmethod lookup-type
  :fully-qualified-array
  [type symbol-table]
  (let [type (string/replace type "[]" "")
        split-type (string/split type #"\.")
        base-type (first split-type)
        symbol-type (get symbol-table base-type)] ; IE Foo in Foo.Bar
    (string/replace type base-type symbol-type)))

(defmethod lookup-type
  :array
  [type symbol-table]
  (let [type (string/replace type "[]" "")
        symbol-type (get symbol-table type)] ; IE Foo in Foo.Bar
    (string/replace type type symbol-type)))

(defmethod lookup-type
  :fully-qualified
  [type symbol-table]
  (let [split-type (string/split type #"\.")
        base-type (first split-type)
        symbol-type (get symbol-table base-type)] ; IE Foo in Foo.Bar
    (string/replace type base-type symbol-type)))

(defn get-array-types
  "Returns a list of all the types that are used as an array that aren't fields"
  [astproto symbol-table]
  (let [modules (:modules astproto)
        signatures (map :signature modules)

        inputs (flatten (map :inputs signatures))
        array-types (filter #(string/ends-with? % "[]") inputs)

        outputs (map :output signatures)
        array-types (concat array-types (filter #(string/ends-with? % "[]") outputs))]
    (map #(lookup-type % symbol-table) array-types)))


(let [table (symbol-table astproto)]
  (get-array-types astproto table))
