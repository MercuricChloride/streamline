(ns streamline.ast-helpers
  (:require [spyglass.streamline.alpha.ast :as ast]
            [clojure.data.json :as json]))

(defn parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(defmulti ->expr
  "Converts a expression into an expression node" first)

(defmethod ->expr :function-call
  [input]
  (let [[_ & input] input
        [[_ function] & args] input]
    (ast/new-Expression {:expression {:function-call {:identifier function
                                                      :arguments (map ->expr args)}}})))

(defmethod ->expr :binary-expression
  [input]
  (ast/new-Expression {:expression {:binary-expression {:op input}}}))

(defmethod ->expr :field-access
  [input]
  (let [[_ & input] input
        [identifier field] input]
    (ast/new-Expression {:expression {:field-access {:target (->expr identifier)
                                                     :field (str field)}}})))

(defmethod ->expr :expr-ident
  [input]
  (ast/new-Expression {:expression {:identifier (second input)}}))

(defmethod ->expr :number
  [input]
  (ast/new-Expression {:expression {:literal {:literal {:int (parse-int (second input))}}}}))

(defmethod ->expr :string
  [input]
  (ast/new-Expression {:expression {:literal {:literal {:str (second input)}}}}))

(defn eval-bool [s]
  (cond
    (= s "true") true
    (= s "false") false
    :else (throw (Exception. (str "Invalid boolean value: " s)))))

(defmethod ->expr :boolean
  [input]
  (ast/new-Expression {:expression {:literal {:literal {:boolean (eval-bool (second input))}}}}))

(defmulti ->function first)

(defmethod ->function :lambda
  [input]
  (let [[_ & lambda] input
        inputs (into [] (butlast lambda))
        expression (last lambda)]
    (ast/new-Function {:function {:lambda {:inputs inputs
                                           :body (->expr expression)}}})))

(defmethod ->function :hof
  [input]
  (let [[_ parent-fn & lambda] input
          inputs (into [] (butlast lambda))
          expression (last lambda)]
   (ast/new-Function {:function {:hof {:parent parent-fn
                                       :inputs inputs
                                       :body (->expr expression)}}})))

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
  (let [[_ type ident signature & pipeline] input]
       (ast/new-ModuleDef {:kind type
                           :identifier ident
                           :signature (->module-signature signature)
                           :pipeline (map ->function pipeline)})))

(defmulti ->abi
  "Converts a parse tree node for a function or event, into it's ABI JSON representation"
  first)

(defmethod ->abi :function-w-return
  [input]
  (let [[_ name [_ & params] [_ & returns]] input]
    ;(ast/new-FunctionAbi
    {:type "function"
     :name name
     :inputs (into [] (map ->abi params))
     :outputs (into [] (map ->abi returns))
     :state-mutability "nonpayable"}))
;)

(defmethod ->abi :function-wo-return
  [input]
  (let [[_ name [_ & params]] input]
    (ast/new-FunctionAbi
     {:type "function"
      :name name
      :inputs (into [] (map ->abi params))
      :outputs []
      :state-mutability "nonpayable"})))

(defmethod ->abi :function-param
  [input]
  (let [[_ type name] input]
    (ast/new-FunctionInput
     {:type type
      :name name})))

(defmethod ->abi :unnamed-return
  [input]
  (let [[_ type] input]
    (ast/new-FunctionInput
     {:type type
      :name ""})))

(defmethod ->abi :event-def
  [input]
  (let [[_ name & params] input]
    (ast/new-EventAbi
     {:type "event"
      :name name
      :inputs (into [] (map ->abi params))
      :anonymous false})))

(defmethod ->abi :indexed-event-param
  [input]
  (let [[_ type name] input]
    (ast/new-EventInput
     {:type type
      :name name
      :indexed true})))

(defmethod ->abi :non-indexed-event-param
  [input]
  (let [[_ type name] input]
    (ast/new-EventInput
     {:type type
      :name name
      :indexed false})))

(defn function-def? [input]
  (or (= (first input) :function-w-return)
      (= (first input) :function-wo-return)))

(defmethod ->abi :interface-def
  [input]
  (let [[_ name & items] input
        events (filter #(= (first %) :event-def) items)
        events (into [] (map ->abi events))
        functions (filter function-def? items)
        functions (into [] (map ->abi functions))
        abi-json (->> (concat events functions)
                      (into [])
                      (json/write-str))]
    (ast/new-ContractAbi
     {:name name
      :abi-json abi-json
      :functions functions
      :events events})))
      
