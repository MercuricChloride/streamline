(ns streamline.templating.yaml.helpers
  (:require
   [clj-yaml.core :as yaml]
   [streamline.ast.helpers :refer [find-child]]
   [camel-snake-kebab.core :as csk]))

(def default-yaml
  {:specVersion "v0.1.0"
   :binaries {:default {:type "wasm/rust-v1"
                        :file "/tmp/streamline/streamline.wasm"}}})

(def default-inputs
  {"Block" {:source "sf.ethereum.type.v2.Block"}
   "Clock" {:source "sf.substreams.v1.Clock"}})

(defn format-input
  "Gets the kind and name of an input for a module"
  [input]
  (if (get default-inputs input)
    (get default-inputs input)
    (let [input-name (:type input) ;; (if (map? (:type input))
                     ;;   (:module (:type input))
                     ;;   (:name input))
          ]
      ;TODO Need to add support for store modules
      {:map input-name})))

(defn generate-module-entry
  "Generates a module entry for the substreams yaml. Requires all the metadata to be attached"
  [module]
  (let [{:keys [:output-type :name :inputs]} (meta module)
        inputs (map format-input inputs)]
    {:name name
     :kind "map"
     :inputs inputs
     :output {:type (str "proto:" output-type)}}))

(defn generate-yaml-protobufs
  [namespace]
  {:files [(str namespace ".proto")]
   :import-paths ["/tmp/streamline/proto/"]})

(defn generate-yaml-package
  [namespace]
  {:name namespace
   :version "0.1.0"
   :url "spygpc.com/streamline"})

(defn generate-event-fn
  [interface-name event]
  (let [{:keys [:name :namespace]} (meta event)
        module-name (str "map_" (csk/->snake_case interface-name) "_" (csk/->snake_case name))
        event-proto (str "proto:" namespace "." name "Array")]
    {:name module-name
     :kind "map"
     :inputs (get default-inputs "Block" )
     :output {:type event-proto}}))

(defn generate-interface-event-fns
  [[_ interface-name & children]]
  (let [events (->> children
                    (filter #(= (first %) :event-def))
                    (map #(generate-event-fn interface-name %)))]
    events))

(defn generate-yaml
  [namespace modules interfaces symbol-table]
  (let [protobuf (generate-yaml-protobufs namespace)
        package (generate-yaml-package namespace)
        modules (map generate-module-entry modules)
        modules (concat (flatten (map generate-interface-event-fns interfaces)) modules )]
    (tap> modules)
    (yaml/generate-string (assoc default-yaml
                                 :package package
                                 :protobuf protobuf
                                 :modules modules) :dumper-options {:flow-style :block})))
