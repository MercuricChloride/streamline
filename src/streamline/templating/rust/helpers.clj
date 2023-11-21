(ns streamline.templating.rust.helpers
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as string]
   [pogonos.core :as pg]))

(defn format-rust-path
  "Converts a protobuf path into it's rust equivalent"
  [path]
  (let [parts (string/split path #"\.")
        path-parts (->> (butlast parts)
                        (mapv csk/->snake_case))
        name (csk/->PascalCase (last parts))
        full-path (conj path-parts name)]
    (string/join "::" full-path)))

(defn array-type-conversion
  [type]
  (pg/render-resource "templates/rust/traits/array-from.mustache" {:type type}))

(defmulti get-conversions first)

(defmethod get-conversions :default
  [input]
  nil)

(defmethod get-conversions :struct-def
  [input]
  (let [{:keys [:namespace :name]} (meta input)
        input-path (str namespace "." name)
        input-type (format-rust-path input-path)]
    (array-type-conversion input-type)))

(defn get-all-conversions
  [parse-tree]
  (->> parse-tree
       (map get-conversions)
       (filter #(not (nil? %)))
       (string/join "\n")))
