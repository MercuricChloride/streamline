(ns streamline.templating.rust.functions
  (:require
   [clojure.string :as string]
   [instaparse.core :as insta]
   [pogonos.core :as pg]
   [streamline.ast.helpers :refer [format-type]]
   [streamline.templating.helpers :refer [->proto-symbol ->snake-case
                                          format-rust-path lookup-symbol]]))

(defmulti ->expr
  "Converts a expression into it's rust expression"
  (fn [node _symbol-table] (first node)))

(defmethod ->expr :default
  [input _]
  nil)

(defmethod ->expr :struct-expression-field
  [input st]
  (let [[_ field-name expr] input
        field-name (->snake-case field-name)
        expr (->expr expr st)]
    (str field-name ": " expr)))

(defmethod ->expr :struct-expression
  [input symbol-table]
  (let [[_ struct-name & fields] input
        fields (string/join ",\n" (map #(->expr % symbol-table) fields))]
    (->> {:name (->proto-symbol struct-name symbol-table)
          :fields fields}
         (pg/render-resource "templates/rust/expr/struct-expression.mustache"))))

(defmethod ->expr :field-access
  [input st]
  (let [[_ & input] input
        [identifier field] input
        field (->snake-case field)]
    (str (->expr identifier st) "." field)))

(defmethod ->expr :expr-ident
  [input _]
  (second input))

(defmethod ->expr :function-call
  [input st]
  (let [[_ & input] input
        [[_ function] & args] input
        args (string/join ", " (map #(->expr % st) args))]
    (str function "(" args ")")))

(defmethod ->expr :binary-op
  [input _]
  (second input))

(defmethod ->expr :binary-expression
  [[_ left op right] st]
  (str (->expr left st) " " (->expr op st) " " (->expr right st)))

(defmethod ->expr :number
  [input st]
  (second input))

(defmethod ->expr :string
  [input st]
  (second input))

(defmethod ->expr :boolean
  [input st]
  (second input))

;; (defn eval-bool [s]
;;   (cond
;;     (= s "true") true
;;     (= s "false") false
;;     :else (throw (Exception. (str "Invalid boolean value: " s)))))

(defn format-fn-inputs
  [inputs]
  (->> inputs
       rest
       (map #(format-type %))
       (string/join ", ")))

(defmulti ->function (fn [node _symbol-table] (first node)))

(defmethod ->function :lambda
  [input st]
  (let [[_ inputs expression] input
        inputs (format-fn-inputs inputs)
        body (->expr expression st)]
    (->> (pg/render-resource
          "templates/rust/functions/lambda.mustache"
          {:inputs inputs
           :body body})
         (str "=> "))))

(defmethod ->function :hof
  [input st]
  (let [[_ parent-fn inputs expression] input
        inputs (format-fn-inputs inputs)
        body (->expr expression st)]
    (->> (pg/render-resource
          "templates/rust/functions/hof.mustache"
          {:parent parent-fn
           :inputs inputs
           :body body})
         (str "=> "))))

(defn create-mfn
  "Converts a map module function into a rust function"
  [module st]
  (let [[_ _kind _name _signature & pipeline] module
        m (meta module)
        name (->snake-case (:name m))
        inputs (as-> m m
                 (:inputs m)
                 (map :name m)
                 (map #(lookup-symbol % st) m)
                 (map #(format-rust-path %) m)
                 (map-indexed (fn [i path] (str "input_" i ": " path)) m)
                 (string/join "," m))
        output-type (format-rust-path (:output-type m))
        body (string/join "\n" (map #(->function % st) pipeline))]
    (pg/render-resource
     "templates/rust/functions/mfn.mustache"
     {:name name
      :inputs inputs
      :output output-type
      :body body})))

(defn create-sfn
  "Converts a store module function into a rust function"
  [module st]
  (let [[_ _kind _name _signature & pipeline] module
        m (meta module)
        name (->snake-case (:name m))
        inputs (as-> m m
                 (:inputs m)
                 (map :name m)
                 (map #(lookup-symbol % st) m)
                 (map #(format-rust-path %) m)
                 (map-indexed (fn [i path] (str "input_" i ": " path)) m)
                 (string/join "," m))
        output-type (format-rust-path (:output-type m))
        body (string/join "\n" (map #(->function % st) pipeline))]
    (pg/render-resource
     "templates/rust/functions/mfn.mustache"
     {:name name
      :inputs inputs
      :output output-type
      :body body})))

(defn create-module
  [module st]
  (let [[_ kind & _] module]
    (case kind
      "mfn" (create-mfn module st)
      "sfn" nil) ;TODO
    ))

(defn create-fn
  [fn-def st]
  (let [[_ _name _signature & pipeline] fn-def
        m (meta fn-def)
        name (->snake-case (:name m))
        inputs (as-> m m
                 (map #(lookup-symbol % st) m)
                 (map #(format-rust-path %) m)
                 (map-indexed (fn [i path] (str "input_" i ": " path)) m)
                 (string/join "," m))
        output-type (format-rust-path (:output-type m))
        body (string/join "\n" (map #(->function % st) pipeline))]
    (pg/render-resource
     "templates/rust/functions/fn.mustache"
     {:name name
      :inputs inputs
      :output output-type
      :body body})))

(defn make-mfn
  [name inputs output pipeline]
  (pg/render-resource
   "templates/rust/functions/mfn.mustache"
   {:name (->snake-case name)
    :inputs inputs
    :output output
    :body pipeline}))

(defn make-sfn
  [name inputs output pipeline]
  (pg/render-resource
   "templates/rust/functions/sfn.mustache"
   {:name (->snake-case name)
    :inputs inputs
    :output output ;; TODO Add logic to change how the store module works. Because the output dictates the kind of store it is.
    :body pipeline}))

(defn make-fn
  [name inputs output pipeline]
  (pg/render-resource
   "templates/rust/functions/function.mustache"
   {:name name
    :inputs inputs
    :output output
    :body pipeline}))

(defn create-functions
  "Generates all the of the rust code for the functions and modules in a streamline file"
  [parse-tree st]
  (as-> parse-tree t
    (insta/transform
     {:module (fn [kind name {:keys [:inputs :output]} pipeline]
                (cond
                  (= kind "mfn") (make-mfn name inputs output pipeline)
                  (= kind "sfn") (make-sfn name inputs output pipeline)))

      :fn-def (fn [name {:keys [:inputs :output]} pipeline]
                (make-fn name inputs output pipeline))

      :fn-signature (fn [inputs output]
                      {:inputs inputs
                       :output output})

      :fn-inputs (fn [& inputs]
                   (->> inputs
                        (map-indexed (fn [index input-type]
                                       (str "input_" index ": " input-type)))
                        (string/join ",")))

      :module-signature (fn [inputs output]
                          {:inputs inputs
                           :output output})

      :module-inputs (fn [& inputs]
                       (->> inputs
                            (map-indexed (fn [index input-type]
                                           (str "input_" index ": " input-type)))
                            (string/join ",")))

      :module-output (fn [output] output)

      :pipeline (fn [& _] "todo!();")

      :chained-module (fn [module-name]
                        (-> module-name
                             (lookup-symbol st)
                             (:output)
                             (format-rust-path)))

      :event-array (fn [& parts]
                     (-> (format-type parts)
                         (lookup-symbol st)
                         (format-rust-path)))

      :type (fn [& parts]
              (-> (format-type parts)
                  (lookup-symbol st)
                  (format-rust-path)))} t)))
