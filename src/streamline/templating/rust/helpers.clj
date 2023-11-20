(ns streamline.templating.rust.helpers
  (:require
   [camel-snake-kebab.core :as csk]
   [clojure.string :as string]))

(defn format-rust-path
  "Converts a protobuf path into it's rust equivalent"
  [path]
  (let [parts (string/split path #"\.")
        path-parts (->> (butlast parts)
                        (mapv csk/->snake_case))
        name (csk/->PascalCase (last parts))
        full-path (conj path-parts name)]
    (string/join "::" full-path)))
