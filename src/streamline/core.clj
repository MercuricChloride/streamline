(ns streamline.core
  (:require
   [clojure.data.json :as json]
   [clojure.java.io :as io]
   [clojure.string :as string]
   [instaparse.core :as insta]
   [streamline.ast.helpers :refer [format-type generate-abi]]
   [streamline.ast.metadata :as metadata :refer [get-namespace]]
   [streamline.ast.parser :refer [parser]]
   [streamline.templating.helpers :refer [lookup-event-array lookup-symbol]]
   [streamline.templating.protobufs.helpers :refer [create-protobuf-defs]]
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

(defn something-test
  [ast symbol-table]
  (insta/transform
   {:interface-def (fn [name & children]
                     {:kind ::interface
                      :name name
                      :events children})
    :event-def (fn [name & params]
                 {:kind ::event
                  :name name
                  :params params})
    :indexed-event-param (fn [type name]
                           {:type type
                            :name name})
    :type (fn [& parts] (lookup-symbol (format-type parts) symbol-table))
    :hof (fn [hof inputs & body] [(symbol hof) body inputs])

    :module (fn [kind name signature pipeline]
              {:kind kind
               :name name
               :signature signature
               :pipeline pipeline})
    :module-signature (fn [inputs output]
                        {:inputs inputs
                         :output output})
    :module-inputs (fn [& inputs]
                     (map (fn [input]
                            ;; if the input is a string, this means it's an module name so we need to look it up
                            ;; else it's an event-input so we don't need to do anything
                            (if (string? input)
                              (let [{:keys [:output :module-kind]} (lookup-symbol input symbol-table)]
                                {:name input
                                 :type output
                                 :module-kind module-kind})
                              input)
                            ) inputs)
                     )
    :event-array (fn [& parts]
                   (let [event-arr (format-type parts)
                         proto-type (lookup-symbol event-arr symbol-table)
                         module-name (lookup-event-array event-arr symbol-table)]
                     {:name module-name
                      :type proto-type
                      :module-kind "mfn"}))
} ast))

(let [[ast symbol-table] (metadata/add-metadata erc721)
      ;abis (generate-abi ast)
      ;; _ (write-abis abis)
      ;ast-ns (get-namespace ast)
      modules (->> ast
                   (filter #(= (first %) :module)))
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
  (something-test erc721 symbol-table))
