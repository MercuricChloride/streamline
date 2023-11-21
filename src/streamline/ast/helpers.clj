(ns streamline.ast.helpers
  (:require [clojure.data.json :as json]
            [clojure.string :as string]))

(defn parse-int [s]
  (Integer/parseInt (re-find #"\A-?\d+" s)))


(defn eval-bool [s]
  (cond
    (= s "true") true
    (= s "false") false
    :else (throw (Exception. (str "Invalid boolean value: " s)))))

(defn format-type
  "Formats a type node into a string"
  [type]
  (let [type (rest type)]
    (if (= (last type) "[]")
      (str (string/join "." (butlast type)) "[]")
      (str (string/join "." type)))))

(defn node-type
  "Returns the type of a node, or nil if it is a string or keyword"
  [node]
  (cond
    (string? node) nil
    (keyword? node) nil
    :else (first node)))

(defn find-child
  "Finds a child node in a parse tree"
  [node child-type]
  (first (filter #(= (node-type %) child-type) node)))

;; (defn ->conversion
;;   "Converts a conversion node into a Conversion protobuf message"
;;   [input]
;;   (let [[_ from to & pipeline] input
;;         from (format-type from)
;;         to (format-type to)
;;         pipeline (map ->function pipeline)]
;;     (ast/new-Conversion {:from from
;;                          :to to
;;                          :pipeline pipeline})))

(defn format-types
  [types]
  (map format-type types))


(defmulti ->abi
  "Converts a parse tree node for a function or event, into it's ABI JSON representation"
  first)

(defmethod ->abi :default
  [input]
  nil)

(defmethod ->abi :function-w-return
  [input]
  (let [[_ name [_ & params] [_ & returns]] input]
    {:type "function"
     :name name
     :inputs (into [] (map ->abi params))
     :outputs (into [] (map ->abi returns))
     :state-mutability "nonpayable"}))
;)

(defmethod ->abi :function-wo-return
  [input]
  (let [[_ name [_ & params]] input]
     {:type "function"
      :name name
      :inputs (into [] (map ->abi params))
      :outputs []
      :state-mutability "nonpayable"}))

(defmethod ->abi :function-param
  [input]
  (let [[_ type name] input]
     {:type (format-type type)
      :name name}))

(defmethod ->abi :unnamed-return
  [input]
  (let [[_ type] input]
     {:type (format-type type)
      :name ""}))

(defmethod ->abi :event-def
  [input]
  (let [[_ name & params] input]
     {:type "event"
      :name name
      :inputs (into [] (map ->abi params))
      :anonymous false}))

(defmethod ->abi :indexed-event-param
  [input]
  (let [[_ type name] input]
     {:type (format-type type)
      :name name
      :indexed true}))

(defmethod ->abi :non-indexed-event-param
  [input]
  (let [[_ type name] input]
     {:type (format-type type)
      :name name
      :indexed false}))

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
    {:name name
     :abi-json abi-json
     :functions functions
     :events events}))

(defn generate-abi
  "Generates an ABI JSON string from a parse tree"
  [parse-tree]
  (->> parse-tree
       (map ->abi)
       (filter #(not= % nil))))
