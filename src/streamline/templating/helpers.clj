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
