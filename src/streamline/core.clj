(ns streamline.core
  (:require [instaparse.core :as insta]
             [streamline.ast-helpers :refer :all]
             [clojure.java.io]
             [sf.substreams.v1 :as sf]
             [spyglass.streamline.alpha.ast :as ast])
  (:use [infix.macros])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def streamline-parser
  (insta/parser
   "<S> = (module / struct-def)*

    lambda = <'('> identifier* <')'> <'=>'> ( (<'{'> expression* <'}'>) / (expression) )
    hof = parent-function <'('> identifier* <')'> <'=>'> ( (<'{'> expression* <'}'>) / (expression) )
    <parent-function> = ('filter' / 'map' / 'reduce' / 'apply')
    <pipeline> = ((lambda / hof) <';'>)*

    <expression> = (function-call / binary-expression / field-access / expr-ident / number)
    expr-ident = identifier
    function-call = expression <'('> expression* <')'>
    binary-op = ('+' / '-' / '*' / '/' / '==' / '!=' / '<' / '>' / '<=' / '>=' / '&&' / '||' / '!')
    binary-expression = expression binary-op expression
    field-access = expression (<'.'> identifier)+

    module = module-type identifier <':'> module-signature <'{'> pipeline  <'}'>
    <module-type> = 'map' | 'store'
    module-signature = <'('> identifier* <')'> <'->'> identifier

    struct-def = <'struct'> identifier <'{'> (struct-field <';'>)* <'}'>
    struct-field = identifier <':'> (solidity-type / custom-type) ('[' ']')?

    solidity-type = 'address' / 'bool' / 'string' / 'bytes' / ('int' number?) / ('uint' number?);
    custom-type = identifier

    <identifier> = #'[a-zA-Z_][a-zA-Z0-9_]*'
    <number> = #'[0-9]+'
    "
   :auto-whitespace :comma))

(streamline-parser "
struct Foo {
    user: address;
    balance: uint256;
    something: Fart;
}
")

(def ast (streamline-parser "
map pools_created:
(Block,Transfer,SomethingElse) -> Pools {
 (block) => foo(bar);
 (block) => foo;
 (block) => foo(bar).baz;
 (block) => 42 + 12 + foo.bar;
}

map something_else:
(Block) -> Pools {
 (block) => logs(block);
 (block transfer) => logs(block);
 filter (block) => logs(block);
 map (block) => logs(block);
 map (transfer,foo) => logs(block);
}
"))

(second (map ->map-module ast))

(ast/new-ModuleDef {:kind "map"
                    :identifier "pools_created"
                    :signature (ast/new-ModuleSignature {:inputs ["foo" "bar"]
                                                         :output "baz"})})

(defmulti eval-expr :expression-type)

(defmethod eval-expr :identifier
    [input]
    (eval (symbol (:ident input))))

(defmethod eval-expr :number
    [input]
    (Integer/parseInt (:number input)))

(defmethod eval-expr :function-call
    [input]
    (let [function-name (:function-name input)
          args (:args input)]
        (apply (eval-expr function-name) (map eval-expr args))))

(defmacro step->
  "Like as->, but only performs the first COUNT forms"
  [expr count & forms]
  (let [forms (take count forms)]
    `(->> ~expr ~@forms)))

;; (defn module?
;;   "Tests if an AST node is a module-def"
;;   [node]
;;   (= (first node) :module))


;; (def modules (map ->map-module (rest ast)))

;; (let [[_ type ident sig body] (first (rest ast))
;;       [_ & lambdas] body]
;;   (map hof? lambdas))

;(->map-module (first  (rest ast)))
