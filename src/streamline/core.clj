(ns streamline.core
  (:require
   [streamline.ast.parser :refer [try-parse]]
   [streamline.bundle :refer [bundle-file]]
   [streamline.templating.rust.helpers :refer [use-statements]])
  (:gen-class))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)]
    (println "Compiling streamline file: " path)
    (bundle-file path)
    (println "Finished compiling streamline file: " path)))
