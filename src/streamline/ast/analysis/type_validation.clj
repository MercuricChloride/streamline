(ns streamline.ast.analysis.type-validation)

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

(defn flatten-types
  [types]
  (into {} (flatten types)))

(defn symbol-table
  "This function creates a symbol table for the ast.
  Going from a identifier -> it's type"
  [ast]
  (let [contracts (:contracts ast)
        types (->> (map contract->types contracts)
                   flatten)
        structs (:types ast)
        types (concat types (structs->types structs))
        table (into {} (flatten types))

        contract-instances (:instances ast)
        types (concat types (instances->types contract-instances table))]
    (into {} (flatten types))))
