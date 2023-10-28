(ns streamline.core
  (:require [instaparse.core :as insta]
             [streamline.ast-helpers :refer :all]
             [clojure.java.io]
             [sf.substreams.v1 :as sf]
             [spyglass.streamline.alpha.ast :as ast])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!"))

(def streamline-parser
  (insta/parser
   ;"S = (module*)
   "S = module*

    lambda = [parent-function] <'('> identifier* <')'> <'=>'> ( (<'{'> expression* <'}'>) / (expression) )
    parent-function = ('filter' / 'map' / 'reduce' / 'apply')

    expression = (function-call / binary-expression / field-access / identifier / number)
    function-call = expression <'('> expression* <')'>
    binary-op = ('+' / '-' / '*' / '/' / '==' / '!=' / '<' / '>' / '<=' / '>=' / '&&' / '||' / '!')
    binary-expression = expression binary-op expression
    field-access = expression (<'.'> identifier)+

    module = module-type identifier <':'> module-signature <'{'> module-body  <'}'>
    module-type = 'map' | 'store'
    module-signature = <'('> identifier* <')'> <'->'> identifier
    module-body = (lambda <';'>)*

    struct-def = 'struct' identifier <'{'> struct-body <'}'>
    struct-body = (struct-field <';'>)*
    struct-field = identifier <':'> identifier

    identifier = #'[a-zA-Z_][a-zA-Z0-9_]*'
    number = #'[0-9]+'
    "
   :auto-whitespace :comma))

(def ast (streamline-parser "
map pools_created:
(Block) -> Pools {
 (block) => foo(bar);
 (block) => foo;
 (block) => foo(bar).baz;
 (block) => 42 + 12 + foo.bar;
}

map something_else:
(Block) -> Pools {
 (block) => logs(block);
 filter (block) => logs(block);
 map (block) => logs(block);
 map (transfer,foo) => logs(block);
}
"))

(def test-fns
   ; this gets us the expression of the lambda
    (map :body (map ->function (->> ast
        rest
        first
        last
        rest))))

(defmulti ->expr first)

(defmethod ->expr :function-call
    [input]
    (let [[_ & input] input
          [[_ function] & args] input
          args (->> args
                    (map second)
                    (map ->expr))]
        
      {:function-name function
       :args (into [] args)}))

(defmethod ->expr :binary-expression
    [input]
    input)

(defmethod ->expr :field-access
    [input]
    (let [[_ & input] input
          [[_ identifier] field] input]
      {:field-accessor (->expr identifier)
       :field (->expr field)}))

(defmethod ->expr :identifier
    [input]
    {:ident (second input)})

(defmethod ->expr :number
    [input]
    {:number (second input)})

(map ->expr test-fns)


;; (defn module?
;;   "Tests if an AST node is a module-def"
;;   [node]
;;   (= (first node) :module))


;; (def modules (map ->map-module (rest ast)))

;; (let [[_ type ident sig body] (first (rest ast))
;;       [_ & lambdas] body]
;;   (map hof? lambdas))

;(->map-module (first  (rest ast)))
