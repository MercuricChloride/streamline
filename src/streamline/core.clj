(ns streamline.core
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [streamline.ast.helpers :refer [generate-abi]]
   [streamline.ast.metadata :as metadata]
   [streamline.ast.parser :refer [parser try-parse]]
   [streamline.templating.protobufs.helpers :refer [build-protobufs]]
   [streamline.templating.rust.functions :refer [create-functions]]
   [streamline.templating.rust.helpers :refer [all-conversions]]
   [streamline.templating.yaml.helpers :refer [generate-yaml]])
  (:gen-class))

(def erc721 (try-parse "examples/erc721.strm"))

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
    (let [path (str "/tmp/streamline/abis/" (:name abi) ".json")
          abi (:abi-json abi)]
      (write-to-path path abi))))


(defn bundle-file
  [path]
  (let [parse-tree (try-parse path)
        namespace (metadata/get-namespace parse-tree)
        symbol-table (metadata/get-symbol-table parse-tree)

        abi-json (generate-abi parse-tree symbol-table)
        yaml (generate-yaml parse-tree symbol-table)
        protobuf (build-protobufs parse-tree symbol-table)
        conversions (all-conversions parse-tree symbol-table)]
    ; write the abis
    (println "Writing contract abis")
    (write-abis abi-json)

    ; write the yaml
    (println "Writing substreams yaml")
    (write-to-path (str "/tmp/streamline/substreams.yaml") yaml)

    ; write the protobuf file
    (println "Writing protobuf definitions")
    (write-to-path (str "/tmp/streamline/proto/" namespace ".proto") protobuf)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)]
    (println "Compiling streamline file: " path)
    (bundle-file path)
    (println "Finished compiling streamline file: " path)))

(let [symbol-table (metadata/get-symbol-table erc721)]
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
  (create-functions erc721 symbol-table))
