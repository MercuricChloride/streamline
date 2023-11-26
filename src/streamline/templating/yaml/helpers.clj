(ns streamline.templating.yaml.helpers
  (:require
   [camel-snake-kebab.core :as csk :refer [->snake_case]]
   [clj-yaml.core :as yaml]
   [instaparse.core :as insta]
   [streamline.ast.helpers :refer [format-type]]
   [streamline.ast.metadata :refer [get-namespace]]
   [streamline.templating.helpers :refer [lookup-event-array lookup-symbol]]))

(def default-yaml
  {:specVersion "v0.1.0"
   :binaries {:default {:type "wasm/rust-v1"
                        :file "/tmp/streamline/streamline.wasm"}}})

(def default-inputs
  {"Block" {:source "sf.ethereum.type.v2.Block"}
   "Clock" {:source "sf.substreams.v1.Clock"}})

(defn generate-yaml-protobufs
  [ast]
  (let [namespace (get-namespace ast)]
    {:files [(str namespace ".proto")]
     :importPaths ["/tmp/streamline/proto/"]}))

(defn generate-yaml-package
  [ast]
  (let [namespace (get-namespace ast)]
    {:name namespace
     :version "0.1.0"
     :url "spygpc.com/streamline"}))

(defn yaml-type?
  [node]
  (get #{:interface-def :module} (first node)))

(defn new-event-fn
  [namespace interface-name name]
  (let [module-name (str "map_" (->snake_case interface-name) "_" (->snake_case name))
        event-proto (str "proto:" namespace "." name "Array")]
    {:name module-name
     :kind "map"
     :inputs (get default-inputs "Block")
     :output {:type event-proto}}))

(defn tree->modules
  [ast symbol-table]
  (let [namespace (get-namespace ast)]
    (as-> ast t
      (filter yaml-type? t)
      (insta/transform
       {:interface-def (fn [name & children]
                         (map #(new-event-fn namespace name %) children))

        :event-def (fn [name & _] name)

        :type (fn [& parts] (lookup-symbol (format-type parts) symbol-table))

        :module (fn [kind name {:keys [:inputs :output]} & _]
                  {:name name
                   :kind (if (= kind "mfn") "map" "store")
                   :inputs inputs
                   :output {:type output}})

        :module-signature (fn [inputs output]
                            {:inputs inputs
                             :output (str "proto:" output)})

        :module-inputs (fn [& inputs]
                         (map (fn [input]
                            ;; if the input is a string, this means it's an module name so we need to look it up
                            ;; else it's an event-input so we don't need to do anything
                                (if (string? input)
                                  (let [{:keys [:module-kind]} (lookup-symbol input symbol-table)
                                        kind (if (= module-kind "mfn") :map :store)]
                                    {kind input})
                                  input)) inputs))
        :event-array (fn [& parts]
                       (let [event-arr (format-type parts)
                             module-name (lookup-event-array event-arr symbol-table)]
                         {:map module-name}))
        :module-output (fn [output]
                         output)} t)
      (flatten t))))

(defn generate-yaml
  [ast symbol-table]
  (let [protobuf (generate-yaml-protobufs ast)
        package (generate-yaml-package ast)
        modules (tree->modules ast symbol-table)]

    (yaml/generate-string (assoc default-yaml
                                 :package package
                                 :protobuf protobuf
                                 :modules modules) :dumper-options {:flow-style :block})))
