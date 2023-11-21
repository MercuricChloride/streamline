(ns streamline.templating.yaml.helpers
  (:require
   [clj-yaml.core :as yaml]
   [clojure.string :as string]
   [streamline.ast.metadata :refer [lookup-symbol]]))

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
    (let [input-name (if (map? (:type input))
                       (:module (:type input))
                       (:name input))]
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

(defn generate-yaml
  [namespace modules]
  (let [protobuf (generate-yaml-protobufs namespace)
        package (generate-yaml-package namespace)
        modules (map generate-module-entry modules)]
    (yaml/generate-string (assoc default-yaml
                                 :package package
                                 :protobuf protobuf
                                 :modules modules) :dumper-options {:flow-style :block})))
