;;;----------------------------------------------------------------------------------
;;; Generated by protoc-gen-clojure.  DO NOT EDIT
;;;
;;; Message Implementation of package sf.firehose.v2
;;;----------------------------------------------------------------------------------
(ns sf.firehose.v2
  (:require [protojure.protobuf.protocol :as pb]
            [protojure.protobuf.serdes.core :as serdes.core]
            [protojure.protobuf.serdes.complex :as serdes.complex]
            [protojure.protobuf.serdes.utils :refer [tag-map]]
            [protojure.protobuf.serdes.stream :as serdes.stream]
            [com.google.protobuf :as com.google.protobuf]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]))

;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; Forward declarations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

(declare cis->SingleBlockRequest)
(declare ecis->SingleBlockRequest)
(declare new-SingleBlockRequest)
(declare cis->SingleBlockRequest-BlockNumber)
(declare ecis->SingleBlockRequest-BlockNumber)
(declare new-SingleBlockRequest-BlockNumber)
(declare cis->SingleBlockRequest-BlockHashAndNumber)
(declare ecis->SingleBlockRequest-BlockHashAndNumber)
(declare new-SingleBlockRequest-BlockHashAndNumber)
(declare cis->SingleBlockRequest-Cursor)
(declare ecis->SingleBlockRequest-Cursor)
(declare new-SingleBlockRequest-Cursor)
(declare cis->SingleBlockResponse)
(declare ecis->SingleBlockResponse)
(declare new-SingleBlockResponse)
(declare cis->Request)
(declare ecis->Request)
(declare new-Request)
(declare cis->Response)
(declare ecis->Response)
(declare new-Response)

;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; Enumerations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

;-----------------------------------------------------------------------------
; ForkStep
;-----------------------------------------------------------------------------
(def ForkStep-default :step-unset)

(def ForkStep-val2label {
  0 :step-unset
  1 :step-new
  2 :step-undo
  3 :step-final})

(def ForkStep-label2val (set/map-invert ForkStep-val2label))

(defn cis->ForkStep [is]
  (let [val (serdes.core/cis->Enum is)]
    (get ForkStep-val2label val val)))

(defn- get-ForkStep [value]
  {:pre [(or (int? value) (contains? ForkStep-label2val value))]}
  (get ForkStep-label2val value value))

(defn write-ForkStep
  ([tag value os] (write-ForkStep tag {:optimize false} value os))
  ([tag options value os]
   (serdes.core/write-Enum tag options (get-ForkStep value) os)))


;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; SingleBlockRequest-reference's oneof Implementations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

(defn convert-SingleBlockRequest-reference [origkeyval]
  (cond
     (get-in origkeyval [:reference :block-number]) (update-in origkeyval [:reference :block-number] new-SingleBlockRequest-BlockNumber)
     (get-in origkeyval [:reference :block-hash-and-number]) (update-in origkeyval [:reference :block-hash-and-number] new-SingleBlockRequest-BlockHashAndNumber)
     (get-in origkeyval [:reference :cursor]) (update-in origkeyval [:reference :cursor] new-SingleBlockRequest-Cursor)
     :default origkeyval))

(defn write-SingleBlockRequest-reference [reference os]
  (let [field (first reference)
        k (when-not (nil? field) (key field))
        v (when-not (nil? field) (val field))]
     (case k
         :block-number (serdes.core/write-embedded 3 v os)
         :block-hash-and-number (serdes.core/write-embedded 4 v os)
         :cursor (serdes.core/write-embedded 5 v os)
         nil)))



;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; Message Implementations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

;-----------------------------------------------------------------------------
; SingleBlockRequest
;-----------------------------------------------------------------------------
(defrecord SingleBlockRequest-record [reference transforms]
  pb/Writer
  (serialize [this os]
    (write-SingleBlockRequest-reference  (:reference this) os)
    (serdes.complex/write-repeated serdes.core/write-embedded 6 (:transforms this) os))
  pb/TypeReflection
  (gettype [this]
    "sf.firehose.v2.SingleBlockRequest"))

(s/def ::SingleBlockRequest-spec (s/keys :opt-un []))
(def SingleBlockRequest-defaults {:transforms [] })

(defn cis->SingleBlockRequest
  "CodedInputStream to SingleBlockRequest"
  [is]
  (->> (tag-map SingleBlockRequest-defaults
         (fn [tag index]
             (case index
               3 [:reference {:block-number (ecis->SingleBlockRequest-BlockNumber is)}]
               4 [:reference {:block-hash-and-number (ecis->SingleBlockRequest-BlockHashAndNumber is)}]
               5 [:reference {:cursor (ecis->SingleBlockRequest-Cursor is)}]
               6 [:transforms (serdes.complex/cis->repeated com.google.protobuf/ecis->Any is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->SingleBlockRequest-record)))

(defn ecis->SingleBlockRequest
  "Embedded CodedInputStream to SingleBlockRequest"
  [is]
  (serdes.core/cis->embedded cis->SingleBlockRequest is))

(defn new-SingleBlockRequest
  "Creates a new instance from a map, similar to map->SingleBlockRequest except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::SingleBlockRequest-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::SingleBlockRequest-spec init))))]}
  (-> (merge SingleBlockRequest-defaults init)
      (cond-> (some? (get init :transforms)) (update :transforms #(map com.google.protobuf/new-Any %)))
      (convert-SingleBlockRequest-reference)
      (map->SingleBlockRequest-record)))

(defn pb->SingleBlockRequest
  "Protobuf to SingleBlockRequest"
  [input]
  (cis->SingleBlockRequest (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record SingleBlockRequest-meta {:type "sf.firehose.v2.SingleBlockRequest" :decoder pb->SingleBlockRequest})

;-----------------------------------------------------------------------------
; SingleBlockRequest-BlockNumber
;-----------------------------------------------------------------------------
(defrecord SingleBlockRequest-BlockNumber-record [num]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-UInt64 1  {:optimize true} (:num this) os))
  pb/TypeReflection
  (gettype [this]
    "sf.firehose.v2.SingleBlockRequest-BlockNumber"))

(s/def :sf.firehose.v2.SingleBlockRequest-BlockNumber/num int?)
(s/def ::SingleBlockRequest-BlockNumber-spec (s/keys :opt-un [:sf.firehose.v2.SingleBlockRequest-BlockNumber/num ]))
(def SingleBlockRequest-BlockNumber-defaults {:num 0 })

(defn cis->SingleBlockRequest-BlockNumber
  "CodedInputStream to SingleBlockRequest-BlockNumber"
  [is]
  (->> (tag-map SingleBlockRequest-BlockNumber-defaults
         (fn [tag index]
             (case index
               1 [:num (serdes.core/cis->UInt64 is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->SingleBlockRequest-BlockNumber-record)))

(defn ecis->SingleBlockRequest-BlockNumber
  "Embedded CodedInputStream to SingleBlockRequest-BlockNumber"
  [is]
  (serdes.core/cis->embedded cis->SingleBlockRequest-BlockNumber is))

(defn new-SingleBlockRequest-BlockNumber
  "Creates a new instance from a map, similar to map->SingleBlockRequest-BlockNumber except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::SingleBlockRequest-BlockNumber-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::SingleBlockRequest-BlockNumber-spec init))))]}
  (-> (merge SingleBlockRequest-BlockNumber-defaults init)
      (map->SingleBlockRequest-BlockNumber-record)))

(defn pb->SingleBlockRequest-BlockNumber
  "Protobuf to SingleBlockRequest-BlockNumber"
  [input]
  (cis->SingleBlockRequest-BlockNumber (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record SingleBlockRequest-BlockNumber-meta {:type "sf.firehose.v2.SingleBlockRequest-BlockNumber" :decoder pb->SingleBlockRequest-BlockNumber})

;-----------------------------------------------------------------------------
; SingleBlockRequest-BlockHashAndNumber
;-----------------------------------------------------------------------------
(defrecord SingleBlockRequest-BlockHashAndNumber-record [num hash]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-UInt64 1  {:optimize true} (:num this) os)
    (serdes.core/write-String 2  {:optimize true} (:hash this) os))
  pb/TypeReflection
  (gettype [this]
    "sf.firehose.v2.SingleBlockRequest-BlockHashAndNumber"))

(s/def :sf.firehose.v2.SingleBlockRequest-BlockHashAndNumber/num int?)
(s/def :sf.firehose.v2.SingleBlockRequest-BlockHashAndNumber/hash string?)
(s/def ::SingleBlockRequest-BlockHashAndNumber-spec (s/keys :opt-un [:sf.firehose.v2.SingleBlockRequest-BlockHashAndNumber/num :sf.firehose.v2.SingleBlockRequest-BlockHashAndNumber/hash ]))
(def SingleBlockRequest-BlockHashAndNumber-defaults {:num 0 :hash "" })

(defn cis->SingleBlockRequest-BlockHashAndNumber
  "CodedInputStream to SingleBlockRequest-BlockHashAndNumber"
  [is]
  (->> (tag-map SingleBlockRequest-BlockHashAndNumber-defaults
         (fn [tag index]
             (case index
               1 [:num (serdes.core/cis->UInt64 is)]
               2 [:hash (serdes.core/cis->String is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->SingleBlockRequest-BlockHashAndNumber-record)))

(defn ecis->SingleBlockRequest-BlockHashAndNumber
  "Embedded CodedInputStream to SingleBlockRequest-BlockHashAndNumber"
  [is]
  (serdes.core/cis->embedded cis->SingleBlockRequest-BlockHashAndNumber is))

(defn new-SingleBlockRequest-BlockHashAndNumber
  "Creates a new instance from a map, similar to map->SingleBlockRequest-BlockHashAndNumber except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::SingleBlockRequest-BlockHashAndNumber-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::SingleBlockRequest-BlockHashAndNumber-spec init))))]}
  (-> (merge SingleBlockRequest-BlockHashAndNumber-defaults init)
      (map->SingleBlockRequest-BlockHashAndNumber-record)))

(defn pb->SingleBlockRequest-BlockHashAndNumber
  "Protobuf to SingleBlockRequest-BlockHashAndNumber"
  [input]
  (cis->SingleBlockRequest-BlockHashAndNumber (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record SingleBlockRequest-BlockHashAndNumber-meta {:type "sf.firehose.v2.SingleBlockRequest-BlockHashAndNumber" :decoder pb->SingleBlockRequest-BlockHashAndNumber})

;-----------------------------------------------------------------------------
; SingleBlockRequest-Cursor
;-----------------------------------------------------------------------------
(defrecord SingleBlockRequest-Cursor-record [cursor]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-String 1  {:optimize true} (:cursor this) os))
  pb/TypeReflection
  (gettype [this]
    "sf.firehose.v2.SingleBlockRequest-Cursor"))

(s/def :sf.firehose.v2.SingleBlockRequest-Cursor/cursor string?)
(s/def ::SingleBlockRequest-Cursor-spec (s/keys :opt-un [:sf.firehose.v2.SingleBlockRequest-Cursor/cursor ]))
(def SingleBlockRequest-Cursor-defaults {:cursor "" })

(defn cis->SingleBlockRequest-Cursor
  "CodedInputStream to SingleBlockRequest-Cursor"
  [is]
  (->> (tag-map SingleBlockRequest-Cursor-defaults
         (fn [tag index]
             (case index
               1 [:cursor (serdes.core/cis->String is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->SingleBlockRequest-Cursor-record)))

(defn ecis->SingleBlockRequest-Cursor
  "Embedded CodedInputStream to SingleBlockRequest-Cursor"
  [is]
  (serdes.core/cis->embedded cis->SingleBlockRequest-Cursor is))

(defn new-SingleBlockRequest-Cursor
  "Creates a new instance from a map, similar to map->SingleBlockRequest-Cursor except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::SingleBlockRequest-Cursor-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::SingleBlockRequest-Cursor-spec init))))]}
  (-> (merge SingleBlockRequest-Cursor-defaults init)
      (map->SingleBlockRequest-Cursor-record)))

(defn pb->SingleBlockRequest-Cursor
  "Protobuf to SingleBlockRequest-Cursor"
  [input]
  (cis->SingleBlockRequest-Cursor (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record SingleBlockRequest-Cursor-meta {:type "sf.firehose.v2.SingleBlockRequest-Cursor" :decoder pb->SingleBlockRequest-Cursor})

;-----------------------------------------------------------------------------
; SingleBlockResponse
;-----------------------------------------------------------------------------
(defrecord SingleBlockResponse-record [block]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-embedded 1 (:block this) os))
  pb/TypeReflection
  (gettype [this]
    "sf.firehose.v2.SingleBlockResponse"))

(s/def ::SingleBlockResponse-spec (s/keys :opt-un []))
(def SingleBlockResponse-defaults {})

(defn cis->SingleBlockResponse
  "CodedInputStream to SingleBlockResponse"
  [is]
  (->> (tag-map SingleBlockResponse-defaults
         (fn [tag index]
             (case index
               1 [:block (com.google.protobuf/ecis->Any is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->SingleBlockResponse-record)))

(defn ecis->SingleBlockResponse
  "Embedded CodedInputStream to SingleBlockResponse"
  [is]
  (serdes.core/cis->embedded cis->SingleBlockResponse is))

(defn new-SingleBlockResponse
  "Creates a new instance from a map, similar to map->SingleBlockResponse except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::SingleBlockResponse-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::SingleBlockResponse-spec init))))]}
  (-> (merge SingleBlockResponse-defaults init)
      (cond-> (some? (get init :block)) (update :block com.google.protobuf/new-Any))
      (map->SingleBlockResponse-record)))

(defn pb->SingleBlockResponse
  "Protobuf to SingleBlockResponse"
  [input]
  (cis->SingleBlockResponse (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record SingleBlockResponse-meta {:type "sf.firehose.v2.SingleBlockResponse" :decoder pb->SingleBlockResponse})

;-----------------------------------------------------------------------------
; Request
;-----------------------------------------------------------------------------
(defrecord Request-record [start-block-num cursor stop-block-num final-blocks-only transforms]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-Int64 1  {:optimize true} (:start-block-num this) os)
    (serdes.core/write-String 2  {:optimize true} (:cursor this) os)
    (serdes.core/write-UInt64 3  {:optimize true} (:stop-block-num this) os)
    (serdes.core/write-Bool 4  {:optimize true} (:final-blocks-only this) os)
    (serdes.complex/write-repeated serdes.core/write-embedded 10 (:transforms this) os))
  pb/TypeReflection
  (gettype [this]
    "sf.firehose.v2.Request"))

(s/def :sf.firehose.v2.Request/start-block-num int?)
(s/def :sf.firehose.v2.Request/cursor string?)
(s/def :sf.firehose.v2.Request/stop-block-num int?)
(s/def :sf.firehose.v2.Request/final-blocks-only boolean?)

(s/def ::Request-spec (s/keys :opt-un [:sf.firehose.v2.Request/start-block-num :sf.firehose.v2.Request/cursor :sf.firehose.v2.Request/stop-block-num :sf.firehose.v2.Request/final-blocks-only ]))
(def Request-defaults {:start-block-num 0 :cursor "" :stop-block-num 0 :final-blocks-only false :transforms [] })

(defn cis->Request
  "CodedInputStream to Request"
  [is]
  (->> (tag-map Request-defaults
         (fn [tag index]
             (case index
               1 [:start-block-num (serdes.core/cis->Int64 is)]
               2 [:cursor (serdes.core/cis->String is)]
               3 [:stop-block-num (serdes.core/cis->UInt64 is)]
               4 [:final-blocks-only (serdes.core/cis->Bool is)]
               10 [:transforms (serdes.complex/cis->repeated com.google.protobuf/ecis->Any is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->Request-record)))

(defn ecis->Request
  "Embedded CodedInputStream to Request"
  [is]
  (serdes.core/cis->embedded cis->Request is))

(defn new-Request
  "Creates a new instance from a map, similar to map->Request except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::Request-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::Request-spec init))))]}
  (-> (merge Request-defaults init)
      (cond-> (some? (get init :transforms)) (update :transforms #(map com.google.protobuf/new-Any %)))
      (map->Request-record)))

(defn pb->Request
  "Protobuf to Request"
  [input]
  (cis->Request (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record Request-meta {:type "sf.firehose.v2.Request" :decoder pb->Request})

;-----------------------------------------------------------------------------
; Response
;-----------------------------------------------------------------------------
(defrecord Response-record [block step cursor]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-embedded 1 (:block this) os)
    (write-ForkStep 6  {:optimize true} (:step this) os)
    (serdes.core/write-String 10  {:optimize true} (:cursor this) os))
  pb/TypeReflection
  (gettype [this]
    "sf.firehose.v2.Response"))

(s/def :sf.firehose.v2.Response/step (s/or :keyword keyword? :int int?))
(s/def :sf.firehose.v2.Response/cursor string?)
(s/def ::Response-spec (s/keys :opt-un [:sf.firehose.v2.Response/step :sf.firehose.v2.Response/cursor ]))
(def Response-defaults {:step ForkStep-default :cursor "" })

(defn cis->Response
  "CodedInputStream to Response"
  [is]
  (->> (tag-map Response-defaults
         (fn [tag index]
             (case index
               1 [:block (com.google.protobuf/ecis->Any is)]
               6 [:step (cis->ForkStep is)]
               10 [:cursor (serdes.core/cis->String is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->Response-record)))

(defn ecis->Response
  "Embedded CodedInputStream to Response"
  [is]
  (serdes.core/cis->embedded cis->Response is))

(defn new-Response
  "Creates a new instance from a map, similar to map->Response except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::Response-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::Response-spec init))))]}
  (-> (merge Response-defaults init)
      (cond-> (some? (get init :block)) (update :block com.google.protobuf/new-Any))
      (map->Response-record)))

(defn pb->Response
  "Protobuf to Response"
  [input]
  (cis->Response (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record Response-meta {:type "sf.firehose.v2.Response" :decoder pb->Response})

