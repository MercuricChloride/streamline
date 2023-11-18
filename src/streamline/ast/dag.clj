(ns streamline.ast.dag
  (:require
   [camel-snake-kebab.core :as csk]))

(defn- contract-edges
  "Creates relations between contracts and their events. Used to build the relations for modules with events as input types."
  [base-ast]
  (->> base-ast
       :contracts
       (map (fn [{:keys [:events :name]}]
              (let [map_fn_name (str "map_" (csk/->snake_case name) "_events")]
                (map (fn [event]
                       [{:source map_fn_name
                         :target (str name "." (:name event))}
                        {:source map_fn_name
                         :target (str name "." (str (:name event) "_Array"))}]) events))))
       flatten))

(defn- contract-nodes
  "Creates the modules that we will generate for each contract, and defines their output type."
  [base-ast]
  (->> base-ast
       :contracts
       (map (fn [{:keys [:name]}]
              [(str "map_" (csk/->snake_case name) "_events") {:output (str name ".Events")
                                                               :kind "map"}]))
       (into {})))

(defn ast-nodes
  "Creates all the nodes for the modules present in an AST."
  [base-ast]
  (let [contracts (contract-nodes base-ast)]
    (->> base-ast
         :modules
         (map (fn [{:keys [:signature :identifier :kind]}]
                [identifier {:output (:output signature)
                             :kind kind}]))
         (into {})
         (merge contracts))))

(defn ast-edges
  "Creates all the edges between modules in the AST."
  [base-ast]
  (let [contract-relations (contract-edges base-ast)]
    (->> base-ast
         :modules
         (map (fn [{:keys [:signature :identifier]}]
                (let [{:keys [:inputs]} signature]
                  (map (fn [input]
                         (let [event-source (->> contract-relations
                                                 (filter #(= input (:target %)))
                                                 first
                                                 :source)]
                           {:source (or event-source input) :target identifier}))
                       inputs))))
         flatten)))

(defn construct-dag
  [base-ast]
  (let [nodes (ast-nodes base-ast)

        edges (ast-edges base-ast)

        module-io (->> nodes
                       (map (fn [[module_name {:keys [:output]}]]
                              (let [inputs (->> edges
                                                (filter #(= (:target %) module_name))
                                                (map :source))
                                    inputs (if (empty? inputs)
                                             ["Block"]
                                             inputs)]
                                {:module module_name
                                 :inputs inputs
                                 :output output}))))]
    module-io))
