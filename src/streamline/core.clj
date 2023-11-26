(ns streamline.core
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [instaparse.core :as insta]
   [streamline.ast.helpers :refer [format-type]]
   [streamline.ast.metadata :as metadata]
   [streamline.ast.parser :refer [parser]]
   [streamline.templating.helpers :refer [lookup-event-array lookup-symbol]]
   [streamline.templating.protobufs.helpers :refer [build-protobufs]])
  (:gen-class))

(def erc721 (parser (slurp "examples/erc721.strm")))

(defn write-to-path
  [path content]
  (let [dir-path (io/file (string/join "/" (butlast (string/split path #"/"))))
        file-path (io/file path)]
    (when-not (.exists dir-path)
      (.mkdirs dir-path))
    (spit file-path content)))

(defn write-abis
  [abis]
  (doseq [abi abis]
    (let [path (str "/tmp/streamline/abis/" (:name abi) ".json")]
      (write-to-path path (json/write-str abi)))))

;; (defn bundle-file
;;   [path]
;;   (let [parse-tree (parser (slurp path))
;;         symbol-table (metadata/get-symbol-table parse-tree)
;;         abi-json (generate-abi parse-tree)
;;         yaml (generate-yaml parse-tree symbol-table)
;;         proto-defs (create-protobuf-defs ast)
;;         conversions (get-all-conversions ast symbol-table)]
;;     ; write the abis
;;     (write-abis abi-json)

;;     ; write the yaml
;;     (write-to-path (str "/tmp/streamline/substreams.yaml") yaml)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)]
    ))
(let [symbol-table (metadata/get-symbol-table erc721)
      ;abis (generate-abi ast)
      ;; _ (write-abis abis)
      ;; modules (->> ast
      ;;              (filter #(= (first %) :module)))
      ;interfaces (->> ast
      ;                (filter #(= (first %) :interface-def)))
      ;fns (->> ast
      ;         (filter #(= (first %) :fn-def))
               ;; (map #(create-fn % symbol-table))
      ;         )
      ;yaml (generate-yaml ast-ns modules interfaces symbol-table)
      ;proto-defs (create-protobuf-defs ast)
      ;conversions (get-all-conversions ast symbol-table)
      ;module-code   (as-> modules m
                      ;(map #(create-module % symbol-table) m)
                      ;(string/join "\n" m))
      ]
  (build-protobufs erc721 symbol-table)
  )
