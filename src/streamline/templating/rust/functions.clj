(ns streamline.templating.rust.functions
  (:require
   [clojure.string :as string]
   [instaparse.core :as insta]
   [pogonos.core :as pg]
   [streamline.ast.helpers :refer [format-type pipeline-transforms]]
   [streamline.templating.helpers :refer [->proto-symbol ->snake-case
                                          format-rust-path lookup-symbol]]))

(defn make-mfn
  [name inputs input-names output pipeline]
  (pg/render-resource
   "templates/rust/functions/mfn.mustache"
   {:name (->snake-case name)
    :inputs inputs
    :input-names input-names
    :output output
    :body pipeline}))

(defn make-sfn
  [name inputs input-names output pipeline]
  (pg/render-resource
   "templates/rust/functions/sfn.mustache"
   {:name (->snake-case name)
    :inputs inputs
    :input-names input-names
    :output output ;; TODO Add logic to change how the store module works. Because the output dictates the kind of store it is.
    :body pipeline}))

(defn make-fn
  [name inputs input-names output pipeline]
  (pg/render-resource
   "templates/rust/functions/function.mustache"
   {:name name
    :inputs inputs
    :input-names input-names
    :output output
    :body pipeline}))

(defn create-functions
  "Generates all the of the rust code for the functions and modules in a streamline file"
  [parse-tree st]
  (as-> parse-tree t
    (insta/transform
     (merge {:module (fn [kind name {:keys [:inputs :input-names :output]} pipeline]
                (cond
                  (= kind "mfn") (make-mfn name inputs input-names output pipeline)
                  (= kind "sfn") (make-sfn name inputs input-names  output pipeline)))

      :fn-def (fn [name {:keys [:inputs :input-names :output]} pipeline]
                (make-fn name inputs input-names output pipeline))

      :fn-signature (fn [{:keys [:inputs :input-names]} output]
                      {:inputs inputs
                       :input-names input-names
                       :output output})

      :fn-inputs (fn [& inputs]
                   {:inputs (->> inputs
                                 (map-indexed (fn [index input-type]
                                                (str "input_" index ": " input-type)))
                                 (string/join ","))
                    :input-names (->> inputs
                                      (map-indexed (fn [index _]
                                                     (str "input_" index)))
                                      (string/join ","))})

      :module-signature (fn [{:keys [:inputs :input-names]} output]
                          {:inputs inputs
                           :input-names input-names
                           :output output})

      :module-inputs (fn [& inputs]
                       {:inputs (->> inputs
                                     (map-indexed (fn [index input-type]
                                                    (str "input_" index ": " input-type)))
                                     (string/join ","))
                        :input-names (->> inputs
                                          (map-indexed (fn [index _]
                                                         (str "input_" index)))
                                          (string/join ","))})

      :module-output (fn [output] output)

      :convert (fn [from to]
                 (str to "::from(" from ")"))

      :chained-module (fn [module-name]
                        (-> module-name
                            (lookup-symbol st)
                            (:output)
                            (format-rust-path)))

      :event-array (fn [& parts]
                     (-> (format-type parts)
                         (lookup-symbol st)
                         (format-rust-path)))

      :type (fn [& parts]
              (-> (format-type parts)
                  (lookup-symbol st)
                  (format-rust-path)))} pipeline-transforms) t)
    (filter string? t)))
