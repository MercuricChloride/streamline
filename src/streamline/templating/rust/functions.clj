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

(defn make-hof
  [parent inputs body]
  (pg/render-resource
   "templates/rust/functions/hof.mustache"
   {:parent parent
    :inputs inputs
    :body body}))

(defn make-lambda
  [inputs body]
  (pg/render-resource
   "templates/rust/functions/lambda.mustache"
   {:inputs inputs
    :body body}))

(defn make-mfn
  [name inputs input-names output pipeline]
  (pg/render-resource
   "templates/rust/functions/mfn.mustache"
   {:name (->snake-case name)
    :inputs inputs
    :input-names input-names
    :output output
    :body pipeline}))

(defn make-sfn
  [name inputs input-names output pipeline]
  (pg/render-resource
   "templates/rust/functions/sfn.mustache"
   {:name (->snake-case name)
    :inputs inputs
    :input-names input-names
    :output output ;; TODO Add logic to change how the store module works. Because the output dictates the kind of store it is.
    :body pipeline}))

(defn make-fn
  [name inputs input-names output pipeline]
  (pg/render-resource
   "templates/rust/functions/function.mustache"
   {:name name
    :inputs inputs
    :input-names input-names
    :output output
    :body pipeline}))

(defn create-functions
  "Generates all the of the rust code for the functions and modules in a streamline file"
  [parse-tree st]
  (as-> parse-tree t
    (insta/transform
     {:module (fn [kind name {:keys [:inputs :input-names :output]} pipeline]
                (cond
                  (= kind "mfn") (make-mfn name inputs input-names output pipeline)
                  (= kind "sfn") (make-sfn name inputs input-names  output pipeline)))

      :fn-def (fn [name {:keys [:inputs :input-names :output]} pipeline]
                (make-fn name inputs input-names output pipeline))

      :fn-signature (fn [{:keys [:inputs :input-names]} output]
                      {:inputs inputs
                       :input-names input-names
                       :output output})

      :fn-inputs (fn [& inputs]
                   {:inputs (->> inputs
                                 (map-indexed (fn [index input-type]
                                                (str "input_" index ": " input-type)))
                                 (string/join ","))
                    :input-names (->> inputs
                                      (map-indexed (fn [index _]
                                                     (str "input_" index)))
                                      (string/join ","))})

      :module-signature (fn [{:keys [:inputs :input-names]} output]
                          {:inputs inputs
                           :input-names input-names
                           :output output})

      :module-inputs (fn [& inputs]
                       {:inputs (->> inputs
                                     (map-indexed (fn [index input-type]
                                                    (str "input_" index ": " input-type)))
                                     (string/join ","))
                        :input-names (->> inputs
                                          (map-indexed (fn [index _]
                                                         (str "input_" index)))
                                          (string/join ","))})

      :module-output (fn [output] output)

      :pipeline (fn [& steps] (string/join "\n" steps))

      :hof (fn [parent {:keys [:inputs :body]}]
             (make-hof parent inputs body))

      :callback (fn [args expr]
                  {:inputs args
                   :body expr})

      :lambda (fn [args expr]
                (make-lambda args expr))

      :convert (fn [from to]
                 (str to "::from(" from ")"))

      :fn-args (fn [& names]
                 (string/join "," names))

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
