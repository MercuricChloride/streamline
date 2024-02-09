(ns streamline.lang.targets.rust-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [streamline.lang.parser :refer [parser]]
   [streamline.lang.targets.rust :refer [->rust sol-type]]))

(def simple-test-code "
mfn doubleNum = a
    |> (a) => a * 2;
")

(defn strip-whitespace
  [s]
  (str/replace s #"\s" ""))

(def output [:S [:top-level-interaction
                 [:module-def
                  [:attributes]
                  [:mfn-def [:identifier "doubleNum"]
                   [:module-inputs [:single-input [:identifier "a"]]]
                   [:pipeline [:lambda [:fn-args [:identifier "a"]] [:binary-expression [:identifier "a"] [:binary-op "*"] [:number "2"]]]]]]]])

(def number [:number "2"])
(def ident [:identifier "a"])
(def string [:string "Hello!"])
(def bool [:boolean "true"])
(def binary-expression [:binary-expression [:identifier "a"] [:binary-op "*"] [:number "2"]])
(def longer-binary-expression [:binary-expression [:identifier "a"] [:binary-op "*"] [:binary-expression [:identifier "a"] [:binary-op "*"] [:binary-expression [:identifier "a"] [:binary-op "*"] [:number "2"]]]])

(def function [:fn-def [:identifier "doubleNum"
                               [:module-inputs [:single-input [:identifier "a"]]]
                               [:pipeline [:lambda [:fn-args [:identifier "a"]] [:binary-expression [:identifier "a"] [:binary-op "*"] [:number "2"]]]]]])

(deftest parser-test
  (is (= (parser simple-test-code)
         output)))

(def rust-output "
  #[substreams::handlers::map]
  fn doubleNum(a: prost_wkt_types::Struct) -> Option<prost_wkt_types::Struct> {
      format_inputs!(a);
      with_map! {output_map,
        let mut output_map = a;
        let output_map = (|(a): (SolidityType)| -> SolidityType { { a * sol_type!(Uint, \"2\") }.into() })(output_map);
      }
  }
")

;; (deftest mfn-generation-test
;;   (is (= (strip-whitespace (->rust simple-test-code))
;;          (strip-whitespace rust-output))))

(deftest basic-node-generation-test
  (is (= (->rust number)
         (sol-type "Uint" "2"))
    "Numbers")

  (is (= (->rust ident)
         "a")
      "Idents")

  (is (= (->rust string)
         (sol-type "String" "Hello!"))
      "Strings")
         

  (is (= (->rust bool)
         (sol-type "Boolean" "1"))
      "Booleans"))

(deftest basic-expressions-test
  (testing "Test of generating basic expressions"

    (testing "Binary Expressions"

      (is (= (strip-whitespace (->rust binary-expression))
             (strip-whitespace "a * sol_type!(Uint, \"2\")")))

      (is (= (strip-whitespace (->rust longer-binary-expression))
             (strip-whitespace "a * a * a * sol_type!(Uint, \"2\")"))))))

(deftest modules-test
  (testing "Tests for generation of modules"

    (testing "Tests for mfns"
      (is (= (->rust function)
             (strip-whitespace "
fn doubleNum(a: SolidityType) -> SolidityType {
    with_map! {output_map,
      let mut output_map = a;
      let output_map = (|(a): (SolidityType)| -> SolidityType { { a * sol_type!(Uint, \"2\") }.into() })(output_map);
    }
}
"))))))
