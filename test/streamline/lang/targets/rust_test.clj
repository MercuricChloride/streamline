(ns streamline.lang.targets.rust-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is]]
   [streamline.lang.parser :refer [parser]]
   [streamline.lang.targets.rust :refer [->rust]]))

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

(->rust output)

;; (deftest parser-test
;;   (is (= (parser simple-test-code)
;;          (list output))))

;; (def rust-output "
;;   #[substreams::handlers::map]
;;   fn doubleNum(a: prost_wkt_types::Struct) -> Option<prost_wkt_types::Struct> {
;;       format_inputs!(a);
;;       with_map! {output_map,
;;         let mut output_map = a;
;;         let output_map = (|(a): (SolidityType)| -> SolidityType { { a * sol_type!(Uint, \"2\") }.into() })(output_map);
;;       }
;;   }
;; ")

;; (deftest mfn-generation-test
;;   (is (= (strip-whitespace (->rust simple-test-code))
;;          (strip-whitespace rust-output))))
