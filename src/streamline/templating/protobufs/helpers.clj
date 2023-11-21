(ns streamline.templating.protobufs.helpers
  (:require
   [clojure.string :as string]
   [pogonos.core :as pg]
   [streamline.ast.metadata :refer [get-namespace protobuf-node?]]
   [streamline.templating.helpers :refer [->snake-case]]))

(defn build-proto-message
  ([name fields]
   (pg/render-resource "templates/proto/messages.mustache" {:name name
                                                            :fields fields}))
  ([name fields _]
   (string/join "\n"
                [(pg/render-resource "templates/proto/messages.mustache" {:name name
                                                                          :fields fields})
                 (pg/render-resource "templates/proto/messages.mustache" {:name (str name "Array")
                                                                          :fields (str "repeated " name " elements = 1;")})])))
(defn build-proto-fields
  "Builds the fields for a protobuf message, requires the fields to be a vector of nodes
   that have a name and type string in their metadata"
  [fields]
  (string/join "\n" (map-indexed (fn [index field]
                                   (let [{:keys [:name :type]} (meta field)
                                         name (->snake-case name)]
                                     (pg/render-resource "templates/proto/field.mustache" (merge (meta field) {:index (inc index) :name name})))

                                   (pg/render-resource "templates/proto/field.mustache" (merge (meta field) {:index (inc index)}))) fields)))
(defmulti ->message
  "Converts a node into a protobuf message"
  (fn [node] (first node)))

(defmethod ->message :default
  [node]
  (if (protobuf-node? node)
    (let [{:keys [:name :fields]} (meta node)]
      (build-proto-message name fields :build-array))
    nil))

(defmethod ->message :event-def
  [node]
  (let [{:keys [:name]} (meta node)
        [_ _ & fields] node
        fields (build-proto-fields fields)]
    (build-proto-message name fields :build-array)))

(defmethod ->message :struct-def
  [node]
  (if (protobuf-node? node)
    (let [{:keys [:name]} (meta node)
          [_ _ & fields] node
          fields (build-proto-fields fields)]
      (build-proto-message name fields :build-array))
    nil))

(defmethod ->message :interface-def
  [node]
  (let [{:keys [:name]} (meta node)
        [_ & children] node
        fields (string/join "\n" (map ->message children))]
    (build-proto-message name fields)))

(defn create-protobuf-defs
  "Creates the protobuf file for a streamline file"
  [parse-tree]
  (let [namespace (get-namespace parse-tree)]
    (pg/render-resource
     "templates/proto/protofile.mustache"
     {:namespace namespace
      :messages (as-> parse-tree t
                  (map ->message t)
                  (string/join "\n" t)
                  (string/replace t (str namespace ".") ""))})));replace all instances of the namespace in fields with nothing
