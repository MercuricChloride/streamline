(ns streamline.templating.rust.helpers
  (:require
   [clojure.pprint :as pprint]
   [clojure.string :as string]
   [pogonos.core :as pg]
   [streamline.ast.helpers :refer [format-type]]
   [streamline.templating.helpers :refer [format-rust-path lookup-symbol]]
   [streamline.templating.rust.functions :refer [->function]]))


(defn array-type-conversion
  [type]
  (pg/render-resource "templates/rust/traits/array-from.mustache" {:type type}))

(defn block->event
  [type]
  (pg/render-resource "templates/rust/traits/block-to-event.mustache" {:event-name type}))

(defn event->event
  [event-name type-name fields]
  (pg/render-resource "templates/rust/traits/event-to-event.mustache" {:event-name event-name :type-name type-name :fields fields}))

(event->event (format-rust-path "contracts.erc721.Transfer") "Transfer", ["from", "to"])

(defmulti get-conversions (fn [input & _] (first input)))

(defmethod get-conversions :default
  [input _]
  nil)

; NOTE For events, we need to convert a block into an array of events
; then convert that array of events into their protobuf representation
(defmethod get-conversions :event-def
  [input _ interface-name]
  (let [{:keys [:namespace :name :params]} (meta input)
        event-path (str "contracts." interface-name "." name)
        input-path (str namespace "." name)
        input-type (format-rust-path input-path)
        event-path (format-rust-path event-path)]
    [(array-type-conversion input-type)
     (block->event event-path)
     (event->event event-path input-path params)]))

(defmethod get-conversions :interface-def
  [input st]
  (let [[_ interface-name & children] input
        {:keys [:namespace :name]} (meta input)
        input-path (str namespace "." name)
        input-type (format-rust-path input-path)
        event-fns (reduce (fn [acc event]
                            (concat acc (get-conversions event st interface-name))) [] children)]
     event-fns))

(defmethod get-conversions :struct-def
  [input _]
  (let [{:keys [:namespace :name]} (meta input)
        input-path (str namespace "." name)
        input-type (format-rust-path input-path)]
    [(array-type-conversion input-type)]))

(defmethod get-conversions :conversion
  [input symbol-table]
  (let [[_ from to & pipeline] input
        from (format-type from)
        ; NOTE the `to` type is a literal type, not a binding.
        ; so we need to look up the path for it.
        to (-> to
               format-type
               (lookup-symbol symbol-table)
               format-rust-path)
        pipeline (string/join "\n" (map #(->function % symbol-table) pipeline))]
      [(pg/render-resource "templates/rust/traits/conversion.mustache" {:from from :to to :pipeline pipeline})]))

(defn get-all-conversions
  [parse-tree symbol-table]
  (->> parse-tree
       (reduce (fn [acc node]
                 (concat acc (get-conversions node symbol-table))) [])
       ))
