(ns streamline.core
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [streamline.ast.helpers :refer [generate-abi]]
   [streamline.ast.metadata :as metadata :refer [get-namespace]]
   [streamline.ast.parser :refer [parser]]
   [streamline.templating.protobufs.helpers :refer [create-protobuf-defs]]
   [streamline.templating.rust.functions :refer [create-module]]
   [streamline.templating.rust.helpers :refer [get-all-conversions]]
   [streamline.templating.yaml.helpers :refer [generate-yaml]])
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

(defn bundle-file
  [path]
  (let [parse-tree (parser (slurp path))
        [ast symbol-table] (metadata/add-metadata parse-tree)
        abi-json (generate-abi ast)
        ast-ns (get-namespace ast)
        modules (->> ast
                     (filter #(= (first %) :module)))
        interfaces (->> ast
                        (filter #(= (first %) :interface-def)))
        yaml (generate-yaml ast-ns modules interfaces symbol-table)
        proto-defs (create-protobuf-defs ast)
        conversions (get-all-conversions ast symbol-table)]
    ; write the abis
    (write-abis abi-json)

    ; write the yaml
    (write-to-path (str "/tmp/streamline/substreams.yaml") yaml)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (let [path (first args)]
    (bundle-file path)))

(let [[ast symbol-table] (metadata/add-metadata erc721)
      abis (generate-abi ast)
      ;; _ (write-abis abis)
      ast-ns (get-namespace ast)
      modules (->> ast
                   (filter #(= (first %) :module)))
      interfaces (->> ast
                      (filter #(= (first %) :interface-def)))
      yaml (generate-yaml ast-ns modules interfaces symbol-table)
      proto-defs (create-protobuf-defs ast)
      conversions (get-all-conversions ast symbol-table)
      fns  (as-> modules m
                       (map #(create-module % symbol-table) m)
                       (string/join "\n" m))
      ]
  fns)
