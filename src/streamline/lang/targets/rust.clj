(ns streamline.lang.targets.rust
  (:require
   [pogonos.core :as pg]
   [clojure.spec.alpha :as s]))

;; Write a spec for checking if a value is one of 4 possible keywords
(s/def ::valid-expansion-context
  #(#{:global :borrow :borrow-mut :owned} %))

(def ^:dynamic expansion-context
  "This dynamic variable changes how certain kinds of templates should be generated. The reason for this is rust is extremely
  specific with how and when we use things. So sometimes we only need a read reference, sometimes mutable read references etc.
  "
  :global)

(def ^:dynamic current-attributes
  "This dynamic variable is used to store the current attributes of the currently expanding node"
  {})

(defmacro context
  "Sets the expansion context for the current scope. Valid expansion contexts are `:global :borrow :borrow-mut :owned`"
  [context & body]
  (assert (s/valid? ::valid-expansion-context context) "Invalid expansion context")
  `(binding [expansion-context ~context]
     ~@body))

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

(template mfn
          [name inputs body]
          [module-inputs "module-inputs" ;(module-inputs inputs)
           formatted-inputs "formatted-inputs" ;(format-inputs inputs)
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

(defmulti ->rust
  "Converts a streamline AST to valid rust code"
  first)
