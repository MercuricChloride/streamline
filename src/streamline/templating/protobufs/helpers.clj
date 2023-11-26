(ns streamline.templating.protobufs.helpers
  (:require
   [clojure.string :as string]
   [instaparse.core :as insta]
   [pogonos.core :as pg]
   [streamline.ast.helpers :refer [format-type]]
   [streamline.ast.metadata :refer [get-namespace protobuf-node?]]
   [streamline.templating.helpers :refer [->snake-case lookup-symbol]]))

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
   that have a name and type string in their map"
  [fields]
  (string/join "\n" (map-indexed (fn [index field]
                                   (let [name (->snake-case (:name field))]
                                     (pg/render-resource "templates/proto/field.mustache" (assoc field :index (inc index) :name name)))) fields)))

(defn build-protobufs
  [parse-tree symbol-table]
  (let [namespace (get-namespace parse-tree)]
    (as-> parse-tree t
      (filter protobuf-node? t)
      (insta/transform
       {:struct-def (fn [name & fields]
                      (build-proto-message name (build-proto-fields fields) :build-array))
        :struct-field (fn [type name]
                        (let [type (lookup-symbol (format-type type) symbol-table)
                              repeated? (if (string/ends-with? type "Array") true false)]
                          {:type type
                           :name name
                           :repeated repeated?}))

        :interface-def (fn [name & events]
                         (build-proto-message name (string/join "\n" events)))

        :event-def (fn [name & params]
                     (build-proto-message name (build-proto-fields params) :build-array))

        :indexed-event-param (fn [type name]
                               (let [type (lookup-symbol (format-type type) symbol-table)
                                     repeated? (if (string/ends-with? type "Array") true false)]
                                 {:type type
                                  :name name
                                  :repeated repeated?
                                  :indexed true}))

        :non-indexed-event-param (fn [type name]
                                   (let [type (lookup-symbol (format-type type) symbol-table)
                                         repeated? (if (string/ends-with? type "Array") true false)]
                                     {:type type
                                      :name name
                                      :repeated repeated?
                                      :indexed true}))} t)
      (string/join "\n" t)
      (string/replace t (str namespace ".") ""))))
