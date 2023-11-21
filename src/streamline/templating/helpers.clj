(ns streamline.templating.helpers
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as string]))

(defn ->snake-case [s]
  (if (nil? s)
    nil
    (-> s
        csk/->snake_case
        (string/replace #"([a-zA-Z])_(\d)" "$1$2"))))


(defn format-rust-path
  "Converts a protobuf path into it's rust equivalent"
  [path]
  (let [parts (string/split path #"\.")
        path-parts (->> (butlast parts)
                        (mapv ->snake-case))
        name (csk/->PascalCase (last parts))
        full-path (conj path-parts name)]
    (string/join "::" full-path)))

(defn solidity-type?
  [type]
  (or (string/starts-with? type "uint")
      (string/starts-with? type "int")
      (string/starts-with? type "bytes")
      (string/starts-with? type "bool")
      (string/starts-with? type "string")
      (string/starts-with? type "address")))

(defn solidity->protobuf-type
  [type]
  (cond (= "bool" type) "boolean"
        (= "string" type) "string"
        :else "string"))

(defn lookup-symbol
  [symbol symbol-table]
  (if (solidity-type? symbol)
    (solidity->protobuf-type symbol)
    (loop [parts (string/split symbol #"\.")
           symbol-table symbol-table]
      (let [resolved-symbol (get symbol-table (first parts))]
        (if (= (count parts) 1)
          resolved-symbol
          (recur (rest parts) resolved-symbol))))))

(defn ->proto-symbol
  [symbol symbol-table]
  (format-rust-path (lookup-symbol symbol symbol-table)))
