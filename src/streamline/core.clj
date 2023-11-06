(ns streamline.core
  (:require [instaparse.core :as insta]
            [streamline.ast-helpers :refer :all]
            [streamline.proto-helpers :refer :all]
            [clojure.java.io :as io]
            [clojure.string :as string]
             ;[sf.substreams.v1 :as sf]
            [clojure.data.json :as json]
            [spyglass.streamline.alpha.ast :as ast]
            [protojure.protobuf :as protojure])
  (:use [infix.macros])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def streamline-parser
  (insta/parser
   "<S> = (module / struct-def / interface-def)*

    lambda = <'('> identifier* <')'> <'=>'> ( (<'{'> (expression <';'>)* <'}'>) / (expression <';'>) )
    hof = parent-function <'('> identifier* <')'> <'=>'> ( (<'{'> (expression <';'>)* <'}'>) / (expression <';'>) )
    <parent-function> = ('filter' / 'map' / 'reduce' / 'apply')
    <pipeline> = (lambda / hof)*

    <expression> = (number / string / function-call / binary-expression / field-access / expr-ident )
    expr-ident = identifier
    function-call = expression <'('> expression* <')'>
    binary-op = ('+' / '-' / '*' / '/' / '==' / '!=' / '<' / '>' / '<=' / '>=' / '&&' / '||' / '!')
    binary-expression = expression binary-op expression
    field-access = expression (<'.'> identifier)+

    module = module-type identifier <':'> module-signature <'{'> pipeline  <'}'>
    <module-type> = 'map' | 'store'
    module-signature = map-module-signature / store-module-signature
    <map-module-signature> = <'('> (identifier array?)* <')'> <'->'> (identifier array?)
    <store-module-signature> = <'('> (identifier array?)* <')'> <'->'> store-update-policy
    <store-update-policy> = ('Set' / 'SetNotExists' / 'Add' / 'Min' / 'Max') <'('> (identifier array?) <')'>

    struct-def = <'struct'> identifier <'{'> (struct-field <';'>)* <'}'>
    struct-field = identifier identifier ('[' ']')?

    interface-def = <'interface'> identifier <'{'> ((event-def / function-def) <';'>)* <'}'>

    event-def = <'event'> identifier <'('> event-param* <')'>
    <event-param> = (non-indexed-event-param / indexed-event-param)
    indexed-event-param = identifier <'indexed'> identifier
    non-indexed-event-param = identifier identifier

    <function-def> = (function-w-return / function-wo-return)
    function-w-return = <'function'> identifier <'('> function-params <')'> <function-modifier*> returns
    function-wo-return = <'function'> identifier <'('> function-params <')'> <function-modifier*>
    function-params = function-param*
    function-param = identifier <location?> identifier
    location = 'memory' / 'storage' / 'calldata'
    function-modifier = visibility / mutability
    visibility = 'public' / 'private' / 'internal' / 'external'
    mutability = 'view' / 'pure'
    returns = <'returns'> <'('> return-param* <')'>
    <return-param> = (named-return / unnamed-return)
    unnamed-return = identifier <location?>
    named-return = identifier <location?> identifier

    <identifier> = #'[a-zA-Z_][a-zA-Z0-9_]*'
    number = #'[0-9]+'
    string = <'\"'> #'[^\"\\n]*' <'\"'>
    boolean = 'true' / 'false'
    array = <'['> <']'>
    "
   :auto-whitespace :comma))

(defn serialize-pb
  "Serializes a protobuf message into a byte array"
  [file]
  (->> file
       protojure/->pb
       (into [])
       byte-array))

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
        module-defs (map ->map-module modules)
        struct-defs (map ->structdef struct-defs)
        interfaces (into [] (map ->abi interfaces))
        abi-json (into [] (map #(interface->abijson %) interfaces))]
    (ast/new-StreamlineFile {:modules module-defs
                             :contracts interfaces
                             :types struct-defs
                             :abi-json abi-json})))

(def ast (streamline-parser (slurp "streamline-test.strm")))

(let [interfaces (filter #(= (first %) :interface-def) ast)
      interfaces (into [] (map ->abi interfaces))]
 (doseq [interface interfaces]
  (let [name (:name interface)
        abi (:abi-json interface)
        path (str "/tmp/spyglass/abis/" name ".json")]
    (spit path abi))))

(let [types (:types (ast->file ast))]
     (structs->protobuf types))

;(def ast-file (protojure/->pb (ast->file ast)))
;(write-file ast-file "streamline-test.cstrm")

;(def interface (first (map ->abi (filter #(= (first %) :interface-def) ast))))

;(interface->abijson interface)

;(spit "interface.json" (json/write-str interface))
