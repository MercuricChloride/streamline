(ns streamline.protobuf.helpers
  (:require [streamline.ast.helpers :refer :all]
            [sf.substreams.v1 :as sf]
            [clojure.string :as string]
            [spyglass.streamline.alpha.ast :as ast]
            [camel-snake-kebab.core :as csk]))

(defn solidity-type->protobuf-type
  [input]
  (cond
    (or (string/starts-with? input "uint")
        (string/starts-with? input "int")
        (string/starts-with? input "address")
        (string/starts-with? input "bytes")) "string"
    :else input))

(def test-struct [:struct-def "Zap" [:struct-field "user" "address[]"] [:struct-field "balance" "uint256"]])

(defn index+field->protobuf
  "Converts a vector of [index :struct-field] into a protobuf field"
  [index field]
  (let [index (inc index)] ; protobufs are 1 indexed bleh
    (str field " = " index ";")))

(defn input->protobuf
  [input]
  (let [type (:type input)
        array? (string/ends-with? type "[]")
        type (solidity-type->protobuf-type type)
        name (csk/->snake_case (:name input))]
    (if array?
      (str "repeated " (string/replace type #"\[\]" "") " " name)
      (str type " " name))))

(defn event->protobuf
  "Converts an EventAbi node into a protobuf message"
  [input]
  (let [name (->> (:name input)
                  csk/->PascalCase)
        inputs (:inputs input)
        inputs (map input->protobuf inputs)
        fields (string/join "\n" (map-indexed index+field->protobuf inputs))]
    (str "message " name "{\n" fields "\n}")))

(defn events->protobuf
  "Converts a list of events into a protobuf message"
  [input]
  (let [events (map event->protobuf input)
        event-message (str "message Events {\n"
                           (string/join "\n" (map-indexed (fn [index event]
                                          (let [event-name (:name event)
                                                field-name (csk/->snake_case event-name)
                                                type-name (csk/->PascalCase event-name)
                                                field-tag (inc index)]
                                          (str "repeated " type-name " " field-name " = " field-tag ";\n")))
                                          input)) "}\n\n")]
    (str (string/join "\n\n" events) "\n\n" event-message)))

(defn array-type->protobuf
  [input]
  (let [name (if (string/includes? input ".")
               (last (string/split input #"\."))
               input)]
    (str "message " name "Array{\n"
         "repeated " name " values = 1;\n"
         "}")))

(defn- struct->protobuf
  "Converts a StructDef node into a protobuf message"
  [input array-types]
  (let [{:keys [:name :fields]} input
        fields (map input->protobuf fields)
        as-array (when (get array-types name)
                     (array-type->protobuf name))
        fields (string/join "\n" (map-indexed index+field->protobuf fields))]
    (str "message " name "{\n" fields "\n}\n\n" as-array)))

(defn structs->protobuf
  [input array-types file-namespace]
  (let [template (slurp "./templates/protobuf.txt")
        types (string/join "\n\n" (map #(struct->protobuf % (into #{} array-types)) input))]
    (-> template
        (string/replace "$$PACKAGE-NAME$$" (str file-namespace ".structs"))
        (string/replace "$$TYPES$$" types))))

(defn contract->protobuf
  "Converts a Contract AST node into a protobuf file"
  [input array-types file-namespace]
  (let [name (:name input)
        array-types (->> array-types
                         (filter #(string/starts-with? % name))
                         (map array-type->protobuf)
                         (string/join "\n\n"))
        events (events->protobuf (:events input))
        protobufs (str events "\n\n" array-types)
        functions (:functions input)
        template (slurp "./templates/protobuf.txt")]
    (-> template
        (string/replace "$$PACKAGE-NAME$$" (str file-namespace "." name))
        (string/replace "$$TYPES$$" protobufs))))
