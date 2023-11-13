(ns streamline.ast.new-parser
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [protojure.protobuf :as protojure]
   [spyglass.streamline.alpha.ast :as ast]
   [streamline.ast.analysis.type-validation :refer [get-array-types
                                                    symbol-table]]
   [streamline.ast.helpers :refer [->abi ->contract-instance ->conversion
                                   ->map-module ->structdef]]))

(defmacro require!
  "Tests a predicate, and if false throws an exception"
  [pred exn]
  `(if (not ~pred)
     (throw (Exception. ~exn))))

(defn parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))

(def file-kinds {"stream" :substream
                 "sink" :sink})

(defmulti ->node first)

(defmethod ->node :type
  [[_ & parts]]
  (if (= (last parts) "[]")
    (str (string/join "." (butlast parts)) "[]")
    (str (string/join "." parts))))

;; ========================================
;; FILE META
;; ========================================

(defmethod ->node :file-meta
  [[_ kind name]]
  (let [kind (get file-kinds kind)]
    (require! kind "Invalid streamline kind")
    (ast/new-FileMeta {:name name
                       :kind kind})))

;; ========================================
;;  STRUCTS
;; ========================================

(defmethod ->node :struct-def
  [[_ name & fields]]
  (let [fields (into [] (map ->node fields))]
    (ast/new-StructDef {:name name
                        :fields fields})))

(defmethod ->node :struct-field
  [[_ type name]]
  (ast/new-StructField {:name name
                        :type (->node type)}))

;; ========================================
;;  CONVERSIONS
;; ========================================

(defmethod ->node :conversion
  [[_ from to & pipeline]]
  (ast/new-Conversion {:from (->node from)
                       :to (->node to)
                       :pipeline (into [] (map ->node pipeline))}))

;; ========================================
;;  MODULES
;; ========================================

(defmethod ->node :module
  [[_ kind name signature & pipeline]]
  (ast/new-ModuleDef {:identifier name
                      :kind kind
                      :signature (->node signature)
                      :pipeline (into [] (map ->node pipeline))}))

(defmethod ->node :module-signature
  [[_ inputs output]]
  (ast/new-ModuleSignature {:inputs (->node inputs)
                            :output (->node output)}))

(defmethod ->node :module-inputs
  [[_ & inputs]]
  (into [] (map ->node inputs)))

(defmethod ->node :module-output
  [[_ output]]
  (->node output))

(defmethod ->node :hof
  [[_ parent-fn inputs body]]
  (ast/new-Function {:function {:hof {:parent parent-fn
                                      :inputs (->node inputs)
                                      :body (->node body)}}}))

(defmethod ->node :lambda
  [[_ inputs body]]
  (ast/new-Function {:function {:lambda {:inputs (->node inputs)
                                         :body (->node body)}}}))

;; ========================================
;;  INTERFACES
;; ========================================

;; TODO I should make an address an expression
(defmethod ->node :contract-instance
  [[_ interface name _ [_ address]]]
  (ast/new-ContractInstance {:contract-interface (->node interface)
                             :address address
                             :instance-name name}))

(defmethod ->node :interface-def
  [[_ name & defs]]
  (let [events (->> defs
                    (filter #(= (first %) :event-def))
                    (map ->node)
                    (into []))

        functions (->> defs
                       (filter #(or (= (first %) :function-w-return)
                                    (= (first %) :function-wo-return)))
                       (map ->node)
                       (into []))]
    (ast/new-ContractAbi {:name name
                          :events events
                          :functions functions})))

(defmethod ->node :event-def
  [[_ name & params]]
  (ast/new-EventAbi {:type "event"
                     :name name
                     :inputs (into [] (map ->node params))}))

(defmethod ->node :indexed-event-param
  [[_ type name]]
  (ast/new-EventInput {:name name
                       :type (->node type)
                       :indexed true}))

(defmethod ->node :non-indexed-event-param
  [[_ type name]]
  (ast/new-EventInput {:name name
                       :type (->node type)
                       :indexed false}))

(defmethod ->node :function-w-return
  [[_ name params returns]]
  (ast/new-FunctionAbi
   {:type "function"
    :name name
    :inputs (into [] (map ->node params))
    :outputs (into [] (map ->node returns))
    :state-mutability "nonpayable"}))

(defmethod ->node :function-wo-return
  [[_ name params]]
  (ast/new-FunctionAbi
   {:type "function"
    :name name
    :inputs (->node params)
    :outputs []
    :state-mutability "nonpayable"}))

(defmethod ->node :function-params
  [[_ & params]]
  (into [] (map ->node params)))

(defmethod ->node :function-param
  [[_ type name]]
  (ast/new-FunctionInput {:type (->node type)
                          :name name}))

;; ========================================
;; EXPRESSIONS
;; ========================================

(defmethod ->node :struct-expression-field
  [input]
  (let [[_ field-name expr] input]
    (ast/new-StructLiteralField {:name field-name
                                 :value (->node expr)})))

(defmethod ->node :struct-expression
  [input]
  (let [[_ struct-name & fields] input
        fields (into [] (map ->node fields))]
    (ast/new-Expression {:expression {:literal {:literal {:struct {:name struct-name
                                                                   :fields fields}}}}})))

(defmethod ->node :function-call
  [input]
  (let [[_ & input] input
        [[_ function] & args] input]
    (ast/new-Expression {:expression {:function-call {:identifier function
                                                      :arguments (map ->node args)}}})))

(defmethod ->node :binary-expression
  [input]
  (ast/new-Expression {:expression {:binary-expression {:op input}}}))

(defmethod ->node :field-access
  [input]
  (let [[_ & input] input
        [identifier field] input]
    (ast/new-Expression {:expression {:field-access {:target (->node identifier)
                                                     :field (str field)}}})))

(defmethod ->node :expr-ident
  [input]
  (ast/new-Expression {:expression {:identifier (second input)}}))

(defmethod ->node :number
  [input]
  (ast/new-Expression {:expression {:literal {:literal {:int (parse-int (second input))}}}}))

(defmethod ->node :string
  [input]
  (ast/new-Expression {:expression {:literal {:literal {:str (second input)}}}}))

(defn eval-bool [s]
  (cond
    (= s "true") true
    (= s "false") false
    :else (throw (Exception. (str "Invalid boolean value: " s)))))

(defmethod ->node :boolean
  [input]
  (ast/new-Expression {:expression {:literal {:literal {:boolean (eval-bool (second input))}}}}))
