(ns streamline.ast.writer
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [protojure.protobuf :as protojure]
   [spyglass.streamline.alpha.ast :as ast]
   [streamline.ast.analysis.type-validation :refer [get-array-types
                                                    symbol-table]]
   [streamline.ast.helpers :refer [->abi ->contract-instance ->conversion
                                   ->map-module ->structdef]]
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

(defn format-module-signatures
  "Replaces [] with Array for module signatures"
  [module-defs]
  (map (fn [module-def]
         (let [signature (:signature module-def)
               inputs (:inputs signature)
               output (:output signature)
               inputs (into [] (map #(string/replace % "[]" "Array") inputs))
               output (string/replace output "[]" "Array")]
           (assoc module-def :signature {:inputs inputs :output output}))) module-defs))

(defn ast->file
  "Converts a streamline parse tree into a StreamlineFile protobuf message"
  [ast]
  (let [module-defs (->> ast
                         (filter #(= (first %) :module))
                         (map ->map-module))

        interfaces (->> ast
                        (filter #(= (first %) :interface-def))
                        (map ->abi))

        struct-defs (->> ast
                         (filter #(= (first %) :struct-def))
                         (map ->structdef))

        contract-instances (->> ast
                               (filter #(= (first %) :contract-instance))
                               (map ->contract-instance))

        array-types (get-array-types module-defs (symbol-table interfaces struct-defs contract-instances))

        conversions (->> ast
                         (filter #(= (first %) :conversion))
                         (map ->conversion))

        abi-json (into [] (map interface->abijson interfaces))

        contract-protobufs (into [] (map #(contract->protobuf % array-types) interfaces))

        struct-protobuf (structs->protobuf struct-defs array-types)

        protobufs (conj contract-protobufs struct-protobuf)]
    (ast/new-StreamlineFile {:modules (format-module-signatures  module-defs)
                             :contracts interfaces
                             :types struct-defs
                             :abi-json abi-json
                             :protobufs protobufs
                             :instances contract-instances
                             :conversions conversions})))

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
