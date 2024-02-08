(ns streamline.lang.targets.clojure
  (:require
   [clojure.edn :as edn]
   [streamline.ast.parser :refer [parser]]
   [streamline.macros :refer [defclj defclj*]]))

(defmulti ->clj
  "Converts a streamline AST to a Clojure AST"
  first)

(defn streamline->clj
  [source]
  (as-> source t
    (parser t)
    (map ->clj t)
    (filter #(not (nil? %)) t)
    (doall t)
    t))

;; NOTE This is a dynamic variable that tells us if we are in a higher order function or not
;; This dictates whether or not to generate a lambda with the apply keyword, or just the lambda
(def ^:dynamic in-hof? false)

(defmethod ->clj :default
  [_]
  nil)

(defclj :top-level-interaction
  [interaction])

(defclj :module-inputs
  [input])

(defclj :single-input
  [input]
  [input])

(defmethod ->clj :multi-input
  [[_ & inputs]]
  (vec (mapcat ->clj inputs)))

(defclj* :identifier
  [ident]
  (symbol ident))

(defclj :module-def
  [attributes module]
  (with-attributes attributes module))

(defclj :mfn-def
  [name inputs pipeline]
  `(do
    (defn ~name
     [~@inputs]
     (->> (list ~@inputs)
          ~@pipeline))
    ~(list 'alter-meta! (list 'var name) 'assoc ':kind ':mfn ':inputs 'inputs)))

(defclj :sfn-def
  [name inputs pipeline]
  `(defn ^{:kind :mfn
           :inputs ~inputs} ~name
     [~@inputs]
     (->> (list ~@inputs)
          ~@pipeline)))

(defclj :fn-def
  [name inputs pipeline]
  `(defn ~name
     [~@inputs]
     (->> (list ~@inputs)
          ~@pipeline)))

(defmethod ->clj :pipeline
  [[_ & applications]]
  (map ->clj applications))

(defclj :lambda
  [args expression]
  (if in-hof?
    `(fn [~@args] ~expression)
    `(apply (fn [~@args] ~expression))))

(defclj* :hof
  [kind lambda]
  (binding [in-hof? true]
    (let [kind (->clj kind)
          lambda (->clj lambda)]
      `(~kind ~lambda))))

(defclj* :parent-function
  [kind]
  (symbol kind))

(defmethod ->clj :fn-args
  [[_ & args]]
  (map ->clj args))

(defmethod ->clj :list-literal
  [[_ & items]]
  `(list ~@(map ->clj items)))
(defmethod ->clj :list-literal
  [[_ & items]]
  `(list ~@(map ->clj items)))

(defclj* :number
  [num]
  (edn/read-string num))

(defclj* :string
  [string]
  (str string))

(defclj* :boolean
  [bool]
  (edn/read-string bool))

(defclj* :binary-op
  [op]
  (cond
    (= "||" op) `or
    (= "&&" op) `and
    :else (symbol op)))

(defclj :binary-expression
  [lh op rh]
  `(~op ~lh ~rh))

(defmethod ->clj :function-call
  [[_ function & args]]
  (let [function (->clj function)
        args (map ->clj args)]
    `(apply ~function (list ~args))))

(defmethod ->clj :do-block
  [[_ & exprs]]
  (let [exprs (map ->clj exprs)]
    `(do ~@exprs)))


