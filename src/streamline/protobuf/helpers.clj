(ns streamline.protobuf.helpers
  (:require [streamline.ast.helpers :refer :all]
            [sf.substreams.v1 :as sf]
            [clojure.string :as string]
            [spyglass.streamline.alpha.ast :as ast]))

(defn solidity-type->protobuf-type
  [input]
  (cond
    (or (string/starts-with? input "uint")
        (string/starts-with? input "int")
        (string/starts-with? input "address")
        (string/starts-with? input "bytes")) "string"
    :else input))

(def test-struct [:struct-def "Zap" [:struct-field "user" "address[]"] [:struct-field "balance" "uint256"]])

(defmulti ->protobuf
  "converts a parse tree node into a protobuf message" first)

(defn index+field->protobuf
  "Converts a vector of [index :struct-field] into a protobuf field"
  [input]
  (let [[index field] input
        index (inc index)] ; protobufs are 1 indexed bleh
    (str field " = " index ";")))

(defmethod ->protobuf :struct-def
  [input]
  (let [[_ struct-name & fields] input
        fields (map ->protobuf fields)
        index+fields (map-indexed vector fields)
        fields (string/join "\n" (map index+field->protobuf index+fields))]
    (str "message " struct-name "{\n" fields "\n}")))

(defn input->protobuf
  [input]
  (let [type (:type input)
        array? (string/ends-with? type "[]")
        type (solidity-type->protobuf-type type)
        name (:name input)]
    (if array?
      (str "repeated " type " " name)
      (str type " " name))))

(defn event->protobuf
  "Converts a EventAbi node into a protobuf message"
  [input]
  (let [name (:name input)
        inputs (:inputs input)
        inputs (map input->protobuf inputs)
        index+inputs (map-indexed vector inputs)
        fields (string/join "\n" (map index+field->protobuf index+inputs))]
    (str "message " name "{\n" fields "\n}")))

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
  (let [name (:name input)
        fields (:fields input)
        fields (map input->protobuf fields)
        index+fields (map-indexed vector fields)
        array-type (when (get array-types name)
                     (array-type->protobuf name))
        fields (string/join "\n" (map index+field->protobuf index+fields))]
    (str "message " name "{\n" fields "\n}\n\n" array-type)))

(defn structs->protobuf
  [input array-types]
  (let [template (slurp "./templates/protobuf.txt")
        types (string/join "\n\n" (map #(struct->protobuf % (into #{} array-types)) input))]
    (-> template
        (string/replace "$$PACKAGE-NAME$$" (str "streamline.test.structs"))
        (string/replace "$$TYPES$$" types))))

(defn contract->protobuf
  "Converts a Contract AST node into a protobuf file"
  [input array-types]
  (let [name (:name input)
        array-types (->> array-types
                         (filter #(string/starts-with? % name))
                         (map array-type->protobuf)
                         (string/join "\n\n"))
        events (string/join "\n\n" (map event->protobuf (:events input)))
        protobufs (str events "\n\n" array-types)
        functions (:functions input)
        template (slurp "./templates/protobuf.txt")]
    (-> template
        (string/replace "$$PACKAGE-NAME$$" (str "streamline.test." name))
        (string/replace "$$TYPES$$" protobufs))))
