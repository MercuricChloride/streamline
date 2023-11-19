(ns streamline.templating.protobuf
  (:require
   [clojure.string :as string]
   [pogonos.core :as pg]
   [streamline.ast.metadata :refer [protobuf-node?]]))

(defn build-proto-message
  [name fields]
  (pg/render-resource "templates/proto/messages.mustache" {:name name
                                                           :fields fields}))
(defmulti ->message
  "Converts a node into a protobuf message"
  (fn [node _symbol-table] (first node)))

(defmethod ->message :default
  [node st]
  (if (protobuf-node? node)
    (let [{:keys [:namespace :name]} (meta node)
          fields "//todo;"]
      (build-proto-message name fields))
    nil))

(defmethod ->message :interface-def
  [node st]
  (let [{:keys [:namespace :name]} (meta node)
        [_ & children] node
        fields (string/join "\n" (map ->message children))]
    (build-proto-message name fields)))

(defn create-protobuf-defs
  "Creates the protobuf file for a streamline file"
  [parse-tree]
  (let [namespace (get-namespace parse-tree)]
    {:namespace namespace
     :messages (string/join "\n" (map ->message parse-tree))}))
