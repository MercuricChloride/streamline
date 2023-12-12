(ns streamline.ast.parser-test
  (:require
   [clojure.test :refer [are deftest is testing]]
   [streamline.ast.parser :as subject]))

(def simple-test-file
  "Contains some events and a struct def. This is a super minimal example of a valid steramline file"
  "
stream minimal_erc721;

interface Erc721 {
    event Transfer(address indexed from, address indexed to, uint256 indexed tokenId);
    event Approval(address indexed owner, address indexed approved, uint256 indexed tokenId);
}
")

(def w-function (str simple-test-file "
fn double_num:
(uint) -> uint {
       (a) => a * 2;
       (a) => a * 2;
       (a) => a * 2;
}"))

(def w-conversion (str w-function "
convert:
Erc721.Transfer -> Burn {
    (transfer) => Burn {
                        burner: transfer.from,
                        token: transfer.tokenId
                      };
}
"))

(def w-complex-module (str w-conversion "
mfn burns:
(Erc721.Transfer[]) -> Burn[] {
    filter (transfer) => transfer.to != address(0);
    map (transfer) => convert(transfer, Burn);
}
"))

(deftest streamline.ast.parser-test
  (testing "Happy path parsing tests"
    (are [input] (seq (subject/parser input))
      simple-test-file
      w-function
      w-conversion
      w-complex-module)))
