(ns streamline.ast-helpers
  (:require [spyglass.streamline.alpha.ast :as ast]))

(defmulti ->expr
  "Converts a expression into an expression node" first)

(defmethod ->expr :function-call
  [input]
  (let [[_ & input] input
        [[_ function] & args] input]
    {:expression {:function-call {:identifier function
                                 :arguments (map ->expr args)}}}))


(defmethod ->expr :binary-expression
  [input]
  {:expression {:binary-expression {:op input}}})

(defmethod ->expr :field-access
  [input]
  (let [[_ & input] input
        [[_ identifier] field] input]
    {:expression {:field-access {
                              :target (->expr identifier)
                              :field (str field)}}}))

(defmethod ->expr :expr-ident
  [input]
  {:expression {:identifier (second input)}})

(defmethod ->expr :number
  [input]
  {:expression {:literal {:int (str (second input))}}})

(defmulti ->function first)

(defmethod ->function :lambda
  [input]
  (let [[_ & lambda] input
        inputs (into [] (butlast lambda))
        expression (last lambda)]
    {:function {:lambda {:inputs inputs
                         :body {:expression (->expr expression)}}}}))

(defmethod ->function :hof
  [input]
  (let [[_ parent-fn & lambda] input
        inputs (into [] (butlast lambda))
        expression (last lambda)]
    {:function {:hof {:parent parent-fn
                      :inputs inputs
                      :body {:expression (->expr expression)}}}}))

(defn ->module-signature
  [input]
  (let [[_ & idents] input
        inputs (butlast idents)
        output (last idents)]
    (ast/new-ModuleSignature {:inputs (into [] inputs)
                              :output output})))

(defn ->map-module
  "Returns an AST node for a module definition"
  [input]
  (let [[type ident signature & pipeline] (rest input)]
    (println pipeline)
    (ast/new-ModuleDef {:kind type
                        :identifier ident
                        :signature (->module-signature signature)
                        :pipeline (map ->function pipeline)})))
