(ns streamline.templating.yaml.helpers
  (:require
   [clj-yaml.core :as yaml]
   [clojure.string :as string]
   [streamline.ast.metadata :refer [lookup-symbol]]))

(def default-yaml
  {:specVersion "v0.1.0"
   :package {:name "streamline_test"
             :version "0.1.0"
             :url "spygpc.com/streamline"}
   :protobuf {:files ["foo.proto"]
              :import-paths ["/tmp/streamline/proto/"]}
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

(yaml/generate-string default-yaml :dumper-options {:flow-style :block})
