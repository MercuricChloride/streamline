(ns streamline.bundle
  (:require
   [clojure.java.io :as io]
   [clojure.string :as string]
   [streamline.ast.helpers :refer [generate-abi]]
   [streamline.ast.metadata :as metadata]
   [streamline.ast.parser :refer [try-parse]]
   [streamline.templating.protobufs.helpers :refer [build-protobufs]]
   [streamline.templating.yaml.helpers :refer [generate-yaml]]))

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
        ;yaml (generate-yaml parse-tree symbol-table)
        ;; protobuf (build-protobufs parse-tree symbol-table)
        ;; conversions (all-conversions parse-tree symbol-table)
        ;; fns (create-functions parse-tree symbol-table)
        ;; use-stmts (use-statements parse-tree)
        ;; lib-rs (str use-stmts fns)
        ]

    [parse-tree symbol-table]
    ;(pprint/pprint lib-rs)
    ; write the abis
    ;(println "Writing contract abis")
    ;(write-abis abi-json)

    ; write the yaml
    ;; (println "Writing substreams yaml")
    ;; (write-to-path (str "/tmp/streamline/substreams.yaml") yaml)

    ; write the protobuf file
    ;(println "Writing protobuf definitions")
    ;(write-to-path (str "/tmp/streamline/proto/" namespace ".proto") protobuf)
    ;
    ; write the conversions file
    ;(println "Writing conversions.rs file")
    ;(write-to-path "/tmp/streamline/src/conversions.rs" conversions)

    ; write the rust file
    ;(println "Writing lib.rs file")
    ;; (write-to-path "/tmp/streamline/src/lib.rs" lib-rs)
    ))
(let [path "examples/erc721.strm"
      parse-tree (try-parse path)
      namespace (metadata/get-namespace parse-tree)
      symbol-table (metadata/get-symbol-table parse-tree)

      abi-json (generate-abi parse-tree symbol-table)
      yaml (generate-yaml parse-tree symbol-table)
      ;protobuf (build-protobufs parse-tree symbol-table)
        ;; conversions (all-conversions parse-tree symbol-table)
        ;; fns (create-functions parse-tree symbol-table)
        ;; use-stmts (use-statements parse-tree)
        ;; lib-rs (str use-stmts fns)
      ]

  [parse-tree symbol-table]
  (build-protobufs parse-tree symbol-table)
  )
