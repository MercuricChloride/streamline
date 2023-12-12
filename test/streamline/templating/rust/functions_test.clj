(ns streamline.templating.rust.functions-test
  (:require
   [clojure.test :refer [deftest is]]
   [streamline.ast.parser :refer [parser]]
   [streamline.templating.rust.functions :as subject]))

(def ast (parser (slurp "examples/erc721.strm")))

(deftest create-functions-test
  (is (seq ast)))
