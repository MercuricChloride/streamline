(ns streamline.templating.helpers
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as string]
   [streamline.ast.helpers :refer [format-type]]))

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
  (let [type (first type)]
    (or (string/starts-with? type "uint")
        (string/starts-with? type "int")
        (string/starts-with? type "bytes")
        (string/starts-with? type "bool")
        (string/starts-with? type "string")
        (string/starts-with? type "address"))))

(defn solidity->protobuf-type
  [type]
  (cond (= "bool" type) "bool"
        (= "string" type) "string"
        :else "string"))

(defn- format-symbol
  [symbol]
  (if (string? symbol)
    (string/split "address" #"\.")
    (format-symbol (format-type symbol))))

(defn lookup-symbol
  [symbol symbol-table & {:keys [:raw-type]}]
  (let [symbol (format-symbol symbol)]
    (if (solidity-type? symbol)
      (if raw-type
        symbol
        (solidity->protobuf-type symbol))
      (loop [parts symbol
             symbol-table symbol-table]
        (let [resolved-symbol (get symbol-table (first parts))]
          (if (= (count parts) 1)
            resolved-symbol
            (recur (rest parts) resolved-symbol)))))))

(defn lookup-event-array
  "Returns the map module name for a event array being used as a module input.
  IE `Erc721.Transfer[]` should resolve to `map_erc721_transfer`"
  [event-array symbol-table]
  (let [event-fns (:event-fns (meta symbol-table))]
    (get event-fns event-array)))

(defn ->proto-symbol
  [symbol symbol-table]
  (format-rust-path (lookup-symbol symbol symbol-table)))
