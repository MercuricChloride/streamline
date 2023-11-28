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

;(def erc721 (try-parse "examples/erc721.strm"))
;; (let [symbol-table (metadata/get-symbol-table erc721)]
;;       ;abis (generate-abi ast)
;;       ;; _ (write-abis abis)
;;       ;; modules (->> ast
;;       ;;              (filter #(= (first %) :module)))
;;       ;interfaces (->> ast
;;       ;                (filter #(= (first %) :interface-def)))
;;       ;fns (->> ast
;;       ;         (filter #(= (first %) :fn-def))
;;                ;; (map #(create-fn % symbol-table))
;;       ;         )
;;       ;yaml (generate-yaml ast-ns modules interfaces symbol-table)
;;       ;proto-defs (create-protobuf-defs ast)
;;       ;conversions (get-all-conversions ast symbol-table)
;;       ;module-code   (as-> modules m
;;                       ;(map #(create-module % symbol-table) m)
;;                       ;(string/join "\n" m))
   (bundle-file "examples/erc721.strm")
