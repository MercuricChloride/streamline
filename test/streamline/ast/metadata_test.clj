(ns streamline.ast.metadata-test
  (:require
   [clojure.test :refer [are deftest testing]]
   [streamline.ast.metadata :as subject]
   [streamline.ast.parser-test :as p]))

;; When testing if a symbol table entry is valid. The main thing we want to make sure
;; 1. The symbol table contains no `nil` entries. As this should never happen.
;; 2. The table entries for modules, should be a map of strings.
(defn valid-symbol-table?
  [input]
  true)

(deftest streamline.ast.metadata-test
  (testing "Happy path symbol table creation"
    (are [input] (valid-symbol-table? (subject/get-symbol-table input))
      p/simple-test-file
      ;p/w-function
      ;p/w-conversion
      ;p/w-complex-module
      )))
