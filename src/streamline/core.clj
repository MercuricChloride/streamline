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
   "S = module*

    lambda = [parent-function] <'('> identifier* <')'> <'=>'> ( (<'{'> expression* <'}'>) / (expression) )
    parent-function = ('filter' / 'map' / 'reduce' / 'apply')

    expression = (function-call / identifier / number)
    function-call = identifier <'('> expression* <')'>

    module = module-type identifier <':'> module-signature <'{'> module-body  <'}'>
    module-type = 'map' | 'store'
    module-signature = <'('> identifier* <')'> <'->'> identifier
    module-body = (lambda <';'>)*

    identifier = #'[a-zA-Z_][a-zA-Z0-9_]*'
    number = #'[0-9]+'
    "
   :auto-whitespace :standard))

(def ast (streamline-parser "
map pools_created:
(Block) -> Pools {
 (block) => logs(block);
 filter (block) => logs(block);
 map (block) => logs(block);
 map (transfer foo) => logs(block);
}

map something_else:
(Block) -> Pools {
 (block) => logs(block);
 filter (block) => logs(block);
 map (block) => logs(block);
 map (transfer foo) => logs(block);
}
"))


(def modules (map ->map-module (rest ast)))

(def lam (first (:expressions (first modules))))
