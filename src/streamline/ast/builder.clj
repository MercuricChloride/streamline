(ns streamline.ast.builder
  (:require
   [clojure.string :as string]
   [streamline.templating.helpers :refer [->snake-case]]))

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

(defmethod ->node :default
  [node]
  node)

(defmethod ->node :type
  [[_ & parts]]
  (if (= (last parts) "[]")
    (str (string/join "." (butlast parts)) "_Array")
    (str (string/join "." parts))))

;; ========================================
;; FILE META
;; ========================================

;; (defmethod ->node :file-meta
;;   [[f kind name]]
;;   (let [kind (get file-kinds kind)]
;;     (require! kind "Invalid streamline kind")
;;     [f kind name]
;;     {:name name
;;      :kind kind}))

;; ========================================
;;  STRUCTS
;; ========================================

;; (defmethod ->node :struct-def
;;   [[_ name & fields]]
;;   (let [fields (into [] (map ->node fields))]
;;     {:name (csk/->Camel_Snake_Case name)
;;      :fields fields}))

;; (defmethod ->node :struct-field
;;   [[_ type name]]
;;   {:name (csk/->snake_case name)
;;    :type (->node type)})

;; ========================================
;;  CONVERSIONS
;; ========================================

;; (defmethod ->node :conversion
;;   [[_ from to & pipeline]]
;;   {:from (->node from)
;;    :to (->node to)
;;    :pipeline (into [] (map ->node pipeline))})

;; ========================================
;;  MODULES
;; ========================================

;; (defmethod ->node :module
;;   [[_ kind name signature & pipeline]]
;;   {:identifier (csk/->snake_case name)
;;    :kind kind
;;    :signature (->node signature)
;;    :pipeline (into [] (map ->node pipeline))})

(defmethod ->node :module-signature
  [[_ inputs output]]
  {:inputs (->node inputs)
   :output (->node output)})

(defmethod ->node :module-inputs
  [[_ & inputs]]
  (into [] (map ->node inputs)))

(defmethod ->node :module-output
  [[_ output]]
  (->node output))

(defmethod ->node :hof
  [[_ parent-fn inputs body]]
  {:function {:hof {:parent parent-fn
                    :inputs (->node inputs)
                    :body (->node body)}}})

(defmethod ->node :lambda
  [[_ inputs body]]
  {:function {:lambda {:inputs (->node inputs)
                       :body (->node body)}}})

;; ========================================
;;  INTERFACES
;; ========================================
;; NOTE I am using a record here because an import statement shouldn't be stored in the AST
;; So we don't need to make a protobuf for it
(defrecord ast-import-statement [import-path rename])

(defmethod ->node :import-statement
  [[_ [_ import-path] rename]]
  (->ast-import-statement import-path rename))

;; TODO I should make an address an expression
(defmethod ->node :contract-instance
  [[_ interface name _ [_ address]]]
  {:contract-interface (->node interface)
   :address address
   :instance-name name})

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
    {:name (->snake-case name)
     :events events
     :functions functions}))

(defmethod ->node :event-def
  [[_ name & params]]
  {:type "event"
   :name name
   :inputs (into [] (map ->node params))
   :anonymous false})

(defmethod ->node :indexed-event-param
  [[_ type name]]
  {:name name
   :type (->node type)
   :indexed true})

(defmethod ->node :non-indexed-event-param
  [[_ type name]]
  {:name name
   :type (->node type)
   :indexed false})

(defmethod ->node :function-w-return
  [[_ name params returns]]
  {:type "function"
   :name name
   :inputs (into [] (map ->node params))
   :outputs (into [] (map ->node returns))
   :state-mutability "nonpayable"})

(defmethod ->node :function-wo-return
  [[_ name params]]
  {:type "function"
   :name name
   :inputs (->node params)
   :outputs []
   :state-mutability "nonpayable"})

(defmethod ->node :function-params
  [[_ & params]]
  (into [] (map ->node params)))

(defmethod ->node :function-param
  [[_ type name]]
  {:type (->node type)
   :name name})

;; ========================================
;; EXPRESSIONS
;; ========================================

(defmethod ->node :array-expression
  [input]
  (let [[_ & elements] input]
    {:expression {:literal {:literal {:array {:elements (into [] (map ->node elements))}}}}}))

(defmethod ->node :struct-expression-field
  [input]
  (let [[_ field-name expr] input]
    {:name field-name
     :value (->node expr)}))

(defmethod ->node :struct-expression
  [input]
  (let [[_ struct-name & fields] input
        fields (into [] (map ->node fields))]
    {:expression {:literal {:literal {:struct {:name struct-name
                                               :fields fields}}}}}))

(defmethod ->node :function-call
  [input]
  (let [[_ & input] input
        [[_ function] & args] input]
    {:expression {:function-call {:identifier function
                                  :arguments (map ->node args)}}}))

(defmethod ->node :binary-expression
  [input]
  {:expression {:binary-expression {:op input}}})

(defmethod ->node :field-access
  [input]
  (let [[_ & input] input
        [identifier field] input]
    {:expression {:field-access {:target (->node identifier)
                                 :field (str field)}}}))

(defmethod ->node :expr-ident
  [input]
  {:expression {:identifier (second input)}})

(defmethod ->node :number
  [input]
  {:expression {:literal {:literal {:int (parse-int (second input))}}}})

(defmethod ->node :string
  [input]
  {:expression {:literal {:literal {:str (second input)}}}})

(defn eval-bool [s]
  (cond
    (= s "true") true
    (= s "false") false
    :else (throw (Exception. (str "Invalid boolean value: " s)))))

(defmethod ->node :boolean
  [input]
  {:expression {:literal {:literal {:boolean (eval-bool (second input))}}}})
