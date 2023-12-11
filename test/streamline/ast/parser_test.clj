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

struct Burn {
       address burner;
       uint256 token;
       SomethingElse bar;
}

")

(def function "
fn double_num:
(uint) -> uint {
       (a) => a * 2;
       (a) => a * 2;
       (a) => a * 2;
}")

(def conversion "
convert:
Erc721.Transfer -> Burn {
    (transfer) => Burn {
                        burner: transfer.from,
                        token: transfer.tokenId
                      };
}
")

(def complex-module "
mfn burns:
(Erc721.Transfer[]) -> Burn[] {
    filter (transfer) => transfer.to != address(0);
    map (transfer) => convert(transfer, Burn);
}
")

(deftest streamline.ast.parser-test
  (let [simple simple-test-file
        w-fn (str simple-test-file function)
        w-conversion (str w-fn conversion)
        w-complex-module (str w-conversion complex-module)]
    (testing "Happy path parsing tests"
      (are [input] (seq (subject/parser input))
        simple-test-file
        w-fn
        w-conversion
        w-complex-module))))
