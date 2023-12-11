(ns streamline.ast.helpers
  (:require
   [clojure.data.json :as json]
   [clojure.string :as string]
   [instaparse.core :as insta]
   [pogonos.core :as pg]))

(defn make-hof
  "Creates the rust code for a higher order function 'step'"
  [parent inputs body]
  (pg/render-resource
   "templates/rust/functions/hof.mustache"
   {:parent parent
    :inputs inputs
    :body body}))

(defn make-lambda
  "Creates the rust code for a lambda function 'step'"
  [inputs body]
  (pg/render-resource
   "templates/rust/functions/lambda.mustache"
   {:inputs inputs
    :body body}))

(def pipeline-transforms
  "Contains the transforms to convert pipeline code into rust code"
  {:pipeline (fn [& steps] (string/join "\n" steps))

   :hof (fn [parent {:keys [:inputs :body]}]
          (make-hof parent inputs body))

   :callback (fn [args expr]
               {:inputs args
                :body expr})

   :lambda (fn [args expr]
             (make-lambda args expr))
   :fn-args (fn [& names]
              (string/join "," names))})

(defn type-node?
  [node-type]
  (#{:fully-qualified-identifier :array-identifier :event-array :chained-module} node-type))

(defn format-type
  "Converts a list of string, which represent each `part` of a
  type node into a string, split by a `.`
  IE `[:type 'Foo' 'Bar' '[]']` would become:
  `Foo.Bar[]`"
  ([type]
   (let [type (if (type-node? (first type)) (rest type) [type])]
     (if (= (last type) "[]")
       (str (string/join "." (butlast type)) "[]")
       (str (string/join "." type))))))

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

(defn generate-abi
  "Generates an ABI JSON string from a parse tree"
  [parse-tree symbol-table]
  (->> parse-tree
       (filter #(= (first %) :interface-def))
       (insta/transform
        {:interface-def (fn [name & events]
                          {:name name
                           :events events
                           :abi-json (json/write-str events)})
         :event-def (fn [name & params]
                      {:type "event"
                       :name name
                       :inputs params
                       :anonymous false})
         :indexed-event-param (fn [type name]
                                {:type (format-type type)
                                 :name name
                                 :indexed true})
         :non-indexed-event-param (fn [type name]
                                    {:type (format-type type)
                                     :name name
                                     :indexed true})})))
