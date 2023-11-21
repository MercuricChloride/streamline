(ns streamline.templating.rust.helpers
  (:require
   [clojure.string :as string]
   [pogonos.core :as pg]
   [streamline.templating.helpers :refer [format-rust-path]]))


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
  [input]
  nil)

; NOTE For events, we need to convert a block into an array of events
; then convert that array of events into their protobuf representation
(defmethod get-conversions :event-def
  [input interface-name]
  (let [{:keys [:namespace :name :params]} (meta input)
        event-path (str "contracts." interface-name "." name)
        input-path (str namespace "." name)
        input-type (format-rust-path input-path)
        event-path (format-rust-path event-path)]
    [(array-type-conversion input-type)
     (block->event event-path)
     (event->event event-path input-path params)]))

(defmethod get-conversions :interface-def
  [input]
  (let [[_ interface-name & children] input
        {:keys [:namespace :name]} (meta input)
        input-path (str namespace "." name)
        input-type (format-rust-path input-path)
        event-fns (reduce (fn [acc event]
                            (concat acc (get-conversions event interface-name))) [] children)]
     event-fns))

(defmethod get-conversions :struct-def
  [input]
  (let [{:keys [:namespace :name]} (meta input)
        input-path (str namespace "." name)
        input-type (format-rust-path input-path)]
    [(array-type-conversion input-type)]))

(defn get-all-conversions
  [parse-tree symbol-table]
  (->> parse-tree
       (reduce (fn [acc node]
                 (concat acc (get-conversions node))) [])
       (string/join "\n")))
