(ns streamline.ast.writer
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [protojure.protobuf :as protojure]
   [spyglass.streamline.alpha.ast :as ast]
   [streamline.ast.helpers :refer [->abi ->contract-instance ->map-module
                                   ->structdef]]
   [streamline.protobuf.helpers :refer [contract->protobuf structs->protobuf]]))

(defn write-file [input path]
  (with-open [o (io/output-stream path)]
    (.write o input)))

(defn interface->abijson
  "Converts an interface definition into an ABI JSON string"
  [interface]
  (let [events (:events interface)
        functions (:functions interface)]
    (->> (concat events functions)
         (into [])
         (json/write-str))))

(defn ast->file
  "Converts a streamline parse tree into a StreamlineFile protobuf message"
  [ast]
  (let [modules (filter #(= (first %) :module) ast)

        interfaces (filter #(= (first %) :interface-def) ast)

        struct-defs (filter #(= (first %) :struct-def) ast)

        contract-instances (filter #(= (first %) :contract-instance) ast)

        contract-instances (into [] (map ->contract-instance contract-instances))

        module-defs (map ->map-module modules)

        struct-defs (map ->structdef struct-defs)

        interfaces (into [] (map ->abi interfaces))

        abi-json (into [] (map #(interface->abijson %) interfaces))

        contract-protobufs (into [] (map contract->protobuf interfaces))

        struct-protobuf (structs->protobuf struct-defs)

        protobufs (conj contract-protobufs struct-protobuf)]

    (ast/new-StreamlineFile {:modules module-defs
                             :contracts interfaces
                             :types struct-defs
                             :abi-json abi-json
                             :protobufs protobufs
                             :instances contract-instances})))

(defn write-ast
  [ast]
  (let [file (protojure/->pb (ast->file ast))]
    (write-file file "./streamline-test.cstrm")))

(defn write-abis
  [ast]
  (let [interfaces (filter #(= (first %) :interface-def) ast)
        interfaces (into [] (map ->abi interfaces))]
    (doseq [interface interfaces]
      (let [name (:name interface)
            abi (:abi-json interface)
            path (str "/tmp/spyglass/abis/" name ".json")]
        (spit path abi)))))
