(ns streamline.templating.rust.helpers
  (:require
   [clojure.string :as string]
   [instaparse.core :as insta]
   [pogonos.core :as pg]
   [streamline.ast.helpers :refer [format-type pipeline-transforms]]
   [streamline.ast.metadata :refer [get-namespace]]
   [streamline.templating.helpers :refer [->snake-case format-rust-path
                                          lookup-symbol]]))

(defn array-type-conversion
  [type]
  (pg/render-resource "templates/rust/traits/array-from.mustache" {:type type}))

(defn block->event
  [type]
  (pg/render-resource "templates/rust/traits/block-to-event.mustache" {:event-name type}))

(defn event->event
  [event-name type-name fields]
  (pg/render-resource "templates/rust/traits/event-to-event.mustache" {:event-name event-name :type-name type-name :fields fields}))

(defn event-conversions
  "Creates the following conversions for an event:
  1. Block -> Event
  2. Event -> EventProto
  3. Vec<EventProto> -> EventProtoArray
  4. Block->EventProtoArray fn"
  [namespace interface event params]
  (let [event-path (str "contracts." interface ".events." event)
        input-path (str namespace "." interface "." event)
        input-type (format-rust-path input-path)
        event-path (format-rust-path event-path)]
    (string/join "\n"
                 [(array-type-conversion input-type)
                  (block->event event-path)
                  (event->event event-path input-path params)])))

(defn create-conversion
  "Creates the code to convert some type into another.
  NOTE: REQUIRES THAT THE INPUTS ARE FORMATTED RUST PATHS"
  [from to pipeline]
  (pg/render-resource "templates/rust/traits/conversion.mustache" {:from from :to to :pipeline pipeline}))

(defn all-conversions
  [parse-tree st]
  (as-> parse-tree t
    (insta/transform
     (merge {:struct-def (fn [name & _]
                           (let [name (format-rust-path (lookup-symbol name st))]
                             (array-type-conversion name)))

             :interface-def (fn [interface-name & events]
                              (let [namespace (get-namespace t)]
                                (->> events
                                     (map (fn [[event-name params]]
                                            (event-conversions namespace interface-name event-name params)))
                                     (string/join))))

             :event-def (fn [name & params]
                          [name params])
             :indexed-event-param (fn [_ name]
                                    (->snake-case name))
             :non-indexed-event-param (fn [_ name]
                                        (->snake-case name))

             :conversion (fn [from to pipeline]
                           (create-conversion from to pipeline))

             :type (fn [& parts]
                     (format-rust-path (lookup-symbol (format-type parts) st)))} pipeline-transforms) t)
    (filter string? t)
    (string/join t)))

(defn use-contracts
  [parse-tree]
  (as-> parse-tree t
    (insta/transform
     {:interface-def (fn [name & _]
                       (let [mod-name (->snake-case name)]
                         (pg/render-resource "templates/rust/use-contract.mustache" {:mod-name mod-name :name name})))} t)
    (filter string? t)
    (pg/render-resource "templates/rust/contract-module.mustache" {:contracts (string/join t)})))

(defn use-statements
  [parse-tree]
  (let [contracts (use-contracts parse-tree)]
    (pg/render-resource "templates/rust/use-statements.mustache" {:contracts contracts})))
