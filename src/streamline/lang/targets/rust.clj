(ns streamline.lang.targets.rust
  (:require
   [pogonos.core :as pg]
   [clojure.spec.alpha :as s]
   [clojure.string :as str]))

(defmulti ->rust
  "Converts a streamline AST to valid rust code"
  first)

;; (defmethod ->rust :default
;;   [& rest]
;;   (throw rest))

;; Write a spec for checking if a value is one of 4 possible keywords
(s/def ::valid-expansion-context
  #(#{:global :borrow :borrow-mut :owned} %))

(def ^:dynamic current-context
  "This dynamic variable changes how certain kinds of templates should be generated. The reason for this is rust is extremely
  specific with how and when we use things. So sometimes we only need a read reference, sometimes mutable read references etc.
  "
  :global)

(def ^:dynamic current-attributes
  "This dynamic variable is used to store the current attributes of the currently expanding node"
  {})

(def ^:dynamic local-var-names
  "This dynamic variable is used to store the local variable names of the currently expanding node"
  {})

(defmacro context
  "Sets the expansion context for the current scope. Valid expansion contexts are `:global :borrow :borrow-mut :owned`"
  [context & body]
  (assert (s/valid? ::valid-expansion-context context) "Invalid expansion context")
  `(binding [current-context ~context]
     ~@body))

(defmacro defrust
  "Creates an instance of a ->rust multimethod for the node type. We remove the first argument from the destructed input, as it is the node type"
  {:clj-kondo/ignore [:unresolved-symbol]}
  ([node-type]
   `(defmethod ->rust ~node-type
       [[~'_ ~'& ~'rest-of-nodes]]
       (let [~'node-count (count ~'rest-of-nodes)]
        (cond
         (= 1 ~'node-count) (->rust (first ~'rest-of-nodes))
         (> 1 ~'node-count) (map ->rust ~'rest-of-nodes)))))
  ([node-type args & body]
   (let [args (vec (concat ['_] args))]
     `(defmethod ->rust ~node-type
       [[~@args]]
       ~@body))))

(defmacro template
  "Creates a fn `name` that accepts the `inputs`, and replaces each instance of a input name in the `template-str`, with the value passed in "
  {:clj-kondo/ignore [:unresolved-symbol]}
  ([name inputs template-str]
   `(template ~name ~inputs [] ~template-str))
  ([name inputs remaps template-str]
   (let [input-map (apply hash-map (mapcat #(list (keyword %) %) inputs))]
     `(defn ~name
        ~inputs
        (let ~remaps
          (pg/render-string ~template-str
                            ~input-map))))))

(defmacro w-sep
   [sep & forms]
   "Executes the forms, and str/joins the last value with the separator"
   `(str/join ~sep
             (do ~@forms)))

(defmacro gen
  "Calls ->rust on all args, and executes body in a new let bindings with those values"
  {:clj-kondo/ignore [:unresolved-symbol]}
  [values & body]
  (let [bindings (vec (mapcat (fn [v] [v (list '->rust v)]) values))]
    `(let ~bindings
       ~@body)))

(template deltas-module-input
          [input]
          "{{input}}: Deltas<DeltaProto<prost_wkt_types::Struct>>")
(template normal-module-input
          [input]
          "{{input}}: prost_wkt_types::Struct")
(template format-input-types
          [inputs]
          "format_inputs!({{inputs}});")

(template mfn
          [name inputs body]
          [module-inputs (->rust inputs)
           formatted-inputs "formatted inputs" ;;(format-input-types inputs)
           initial-value "initial-value" ;;(output-map-init inputs)
           local-vars "attr-vars"] ;;(attr-vars)
          "
  #[substreams::handlers::map]
  fn {{name}}({{module-inputs}}) -> Option<prost_wkt_types::Struct> {
      {{local-vars}}
      {{formatted-inputs}}
      with_map! {output_map,
        {{initial-value}}
        {{body}}
    }
  }")

(defrust :pipeline
  [& a]
  "PIPELINE")

(defrust :S)
(defrust :top-level-interaction)
(defrust :module-def
  [attributes module]
  (gen [module]
    module))
(defrust :attributes)
(defrust :module-inputs)
(defrust :single-input)
(defrust :identifier
  [ident]
  (symbol ident))

;; NOTE We are don't call ->rust on the inputs here, but in the templating function,
;; this because we still need to read the ast nodes to figure out what input type
;; the are to the rust function. IE a normal input or a deltas input
(defrust :mfn-def
  [name inputs pipeline]
  (gen [name pipeline]
      (mfn name inputs pipeline)))
