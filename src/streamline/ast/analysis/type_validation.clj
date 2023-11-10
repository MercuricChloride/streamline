(ns streamline.ast.analysis.type-validation
  (:require
   [clojure.string :as string]))

(defn events->types
  [contract-name events]
  (map (fn [event]
         (let [path (str contract-name "." (:name event))]{path path})) events))

(defn contract->types
  [contract]
  (let [name (:name contract)
        events (:events contract)]
    (conj (events->types name events) {name name})))

(defn structs->types
  [structs]
  (map (fn [struct] (let [name (:name struct)]
                     {name name})) structs))

(defn instances->types
  [contract-instances symbol-table]
  (map (fn [instance] (let [name (:instance-name instance)
                           instance-type (:contract-interface instance)
                           type (symbol-table instance-type)]
                        {name type})) contract-instances))

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
  [module-defs symbol-table]
  (let [signatures (map :signature module-defs)

        inputs (flatten (map :inputs signatures))
        array-types (filter #(string/ends-with? % "[]") inputs)

        outputs (map :output signatures)
        array-types (concat array-types (filter #(string/ends-with? % "[]") outputs))]
    (map #(lookup-type % symbol-table) array-types)))

(defn flatten-types
  [types]
  (into {} (flatten types)))

(defn symbol-table
  "This function creates a symbol table for the ast.
  Going from a identifier -> it's type"
  [interfaces struct-defs contract-instances]
  (let [types (->> (map contract->types interfaces)
                   flatten)
        types (concat types (structs->types struct-defs))
        table (into {} (flatten types))

        types (concat types (instances->types contract-instances table))]
    (into {} (flatten types))))
