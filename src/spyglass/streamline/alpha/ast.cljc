;;;----------------------------------------------------------------------------------
;;; Generated by protoc-gen-clojure.  DO NOT EDIT
;;;
;;; Message Implementation of package spyglass.streamline.alpha.ast
;;;----------------------------------------------------------------------------------
(ns spyglass.streamline.alpha.ast
  (:require [protojure.protobuf.protocol :as pb]
            [protojure.protobuf.serdes.core :as serdes.core]
            [protojure.protobuf.serdes.complex :as serdes.complex]
            [protojure.protobuf.serdes.utils :refer [tag-map]]
            [protojure.protobuf.serdes.stream :as serdes.stream]
            [clojure.set :as set]
            [clojure.spec.alpha :as s]))

;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; Forward declarations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

(declare cis->TypeField)
(declare ecis->TypeField)
(declare new-TypeField)
(declare cis->FieldAccess)
(declare ecis->FieldAccess)
(declare new-FieldAccess)
(declare cis->BinaryOp)
(declare ecis->BinaryOp)
(declare new-BinaryOp)
(declare cis->Expression)
(declare ecis->Expression)
(declare new-Expression)
(declare cis->Lambda)
(declare ecis->Lambda)
(declare new-Lambda)
(declare cis->StreamlineFile)
(declare ecis->StreamlineFile)
(declare new-StreamlineFile)
(declare cis->FunctionCall)
(declare ecis->FunctionCall)
(declare new-FunctionCall)
(declare cis->Function)
(declare ecis->Function)
(declare new-Function)
(declare cis->TypeDeclaration)
(declare ecis->TypeDeclaration)
(declare new-TypeDeclaration)
(declare cis->Hof)
(declare ecis->Hof)
(declare new-Hof)
(declare cis->ModuleDef)
(declare ecis->ModuleDef)
(declare new-ModuleDef)
(declare cis->ModuleSignature)
(declare ecis->ModuleSignature)
(declare new-ModuleSignature)
(declare cis->Literal)
(declare ecis->Literal)
(declare new-Literal)

;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; Expression-expression's oneof Implementations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

(defn convert-Expression-expression [origkeyval]
  (cond
     (get-in origkeyval [:expression :literal]) (update-in origkeyval [:expression :literal] new-Literal)
     (get-in origkeyval [:expression :identifier]) origkeyval
     (get-in origkeyval [:expression :function-call]) (update-in origkeyval [:expression :function-call] new-FunctionCall)
     (get-in origkeyval [:expression :binary-op]) (update-in origkeyval [:expression :binary-op] new-BinaryOp)
     (get-in origkeyval [:expression :field-access]) (update-in origkeyval [:expression :field-access] new-FieldAccess)
     :default origkeyval))

(defn write-Expression-expression [expression os]
  (let [field (first expression)
        k (when-not (nil? field) (key field))
        v (when-not (nil? field) (val field))]
     (case k
         :literal (serdes.core/write-embedded 1 v os)
         :identifier (serdes.core/write-String 2  {:optimize false} v os)
         :function-call (serdes.core/write-embedded 3 v os)
         :binary-op (serdes.core/write-embedded 4 v os)
         :field-access (serdes.core/write-embedded 5 v os)
         nil)))


;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; Function-function's oneof Implementations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

(defn convert-Function-function [origkeyval]
  (cond
     (get-in origkeyval [:function :lambda]) (update-in origkeyval [:function :lambda] new-Lambda)
     (get-in origkeyval [:function :hof]) (update-in origkeyval [:function :hof] new-Hof)
     :default origkeyval))

(defn write-Function-function [function os]
  (let [field (first function)
        k (when-not (nil? field) (key field))
        v (when-not (nil? field) (val field))]
     (case k
         :lambda (serdes.core/write-embedded 1 v os)
         :hof (serdes.core/write-embedded 2 v os)
         nil)))


;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; Literal-literal's oneof Implementations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

(defn convert-Literal-literal [origkeyval]
  (cond
     (get-in origkeyval [:literal :int]) origkeyval
     (get-in origkeyval [:literal :str]) origkeyval
     (get-in origkeyval [:literal :boolean]) origkeyval
     :default origkeyval))

(defn write-Literal-literal [literal os]
  (let [field (first literal)
        k (when-not (nil? field) (key field))
        v (when-not (nil? field) (val field))]
     (case k
         :int (serdes.core/write-Int64 1  {:optimize false} v os)
         :str (serdes.core/write-String 2  {:optimize false} v os)
         :boolean (serdes.core/write-Bool 3  {:optimize false} v os)
         nil)))



;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------
;; Message Implementations
;;----------------------------------------------------------------------------------
;;----------------------------------------------------------------------------------

;-----------------------------------------------------------------------------
; TypeField
;-----------------------------------------------------------------------------
(defrecord TypeField-record [field-name field-type]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-String 1  {:optimize true} (:field-name this) os)
    (serdes.core/write-String 2  {:optimize true} (:field-type this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.TypeField"))

(s/def :spyglass.streamline.alpha.ast.TypeField/field-name string?)
(s/def :spyglass.streamline.alpha.ast.TypeField/field-type string?)
(s/def ::TypeField-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.TypeField/field-name :spyglass.streamline.alpha.ast.TypeField/field-type ]))
(def TypeField-defaults {:field-name "" :field-type "" })

(defn cis->TypeField
  "CodedInputStream to TypeField"
  [is]
  (->> (tag-map TypeField-defaults
         (fn [tag index]
             (case index
               1 [:field-name (serdes.core/cis->String is)]
               2 [:field-type (serdes.core/cis->String is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->TypeField-record)))

(defn ecis->TypeField
  "Embedded CodedInputStream to TypeField"
  [is]
  (serdes.core/cis->embedded cis->TypeField is))

(defn new-TypeField
  "Creates a new instance from a map, similar to map->TypeField except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::TypeField-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::TypeField-spec init))))]}
  (-> (merge TypeField-defaults init)
      (map->TypeField-record)))

(defn pb->TypeField
  "Protobuf to TypeField"
  [input]
  (cis->TypeField (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record TypeField-meta {:type "spyglass.streamline.alpha.ast.TypeField" :decoder pb->TypeField})

;-----------------------------------------------------------------------------
; FieldAccess
;-----------------------------------------------------------------------------
(defrecord FieldAccess-record [target field]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-embedded 1 (:target this) os)
    (serdes.core/write-String 2  {:optimize true} (:field this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.FieldAccess"))

(s/def :spyglass.streamline.alpha.ast.FieldAccess/field string?)
(s/def ::FieldAccess-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.FieldAccess/field ]))
(def FieldAccess-defaults {:field "" })

(defn cis->FieldAccess
  "CodedInputStream to FieldAccess"
  [is]
  (->> (tag-map FieldAccess-defaults
         (fn [tag index]
             (case index
               1 [:target (ecis->Expression is)]
               2 [:field (serdes.core/cis->String is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->FieldAccess-record)))

(defn ecis->FieldAccess
  "Embedded CodedInputStream to FieldAccess"
  [is]
  (serdes.core/cis->embedded cis->FieldAccess is))

(defn new-FieldAccess
  "Creates a new instance from a map, similar to map->FieldAccess except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::FieldAccess-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::FieldAccess-spec init))))]}
  (-> (merge FieldAccess-defaults init)
      (cond-> (some? (get init :target)) (update :target new-Expression))
      (map->FieldAccess-record)))

(defn pb->FieldAccess
  "Protobuf to FieldAccess"
  [input]
  (cis->FieldAccess (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record FieldAccess-meta {:type "spyglass.streamline.alpha.ast.FieldAccess" :decoder pb->FieldAccess})

;-----------------------------------------------------------------------------
; BinaryOp
;-----------------------------------------------------------------------------
(defrecord BinaryOp-record [op]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-String 1  {:optimize true} (:op this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.BinaryOp"))

(s/def :spyglass.streamline.alpha.ast.BinaryOp/op string?)
(s/def ::BinaryOp-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.BinaryOp/op ]))
(def BinaryOp-defaults {:op "" })

(defn cis->BinaryOp
  "CodedInputStream to BinaryOp"
  [is]
  (->> (tag-map BinaryOp-defaults
         (fn [tag index]
             (case index
               1 [:op (serdes.core/cis->String is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->BinaryOp-record)))

(defn ecis->BinaryOp
  "Embedded CodedInputStream to BinaryOp"
  [is]
  (serdes.core/cis->embedded cis->BinaryOp is))

(defn new-BinaryOp
  "Creates a new instance from a map, similar to map->BinaryOp except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::BinaryOp-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::BinaryOp-spec init))))]}
  (-> (merge BinaryOp-defaults init)
      (map->BinaryOp-record)))

(defn pb->BinaryOp
  "Protobuf to BinaryOp"
  [input]
  (cis->BinaryOp (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record BinaryOp-meta {:type "spyglass.streamline.alpha.ast.BinaryOp" :decoder pb->BinaryOp})

;-----------------------------------------------------------------------------
; Expression
;-----------------------------------------------------------------------------
(defrecord Expression-record [expression]
  pb/Writer
  (serialize [this os]
    (write-Expression-expression  (:expression this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.Expression"))

(s/def ::Expression-spec (s/keys :opt-un []))
(def Expression-defaults {})

(defn cis->Expression
  "CodedInputStream to Expression"
  [is]
  (->> (tag-map Expression-defaults
         (fn [tag index]
             (case index
               1 [:expression {:literal (ecis->Literal is)}]
               2 [:expression {:identifier (serdes.core/cis->String is)}]
               3 [:expression {:function-call (ecis->FunctionCall is)}]
               4 [:expression {:binary-op (ecis->BinaryOp is)}]
               5 [:expression {:field-access (ecis->FieldAccess is)}]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->Expression-record)))

(defn ecis->Expression
  "Embedded CodedInputStream to Expression"
  [is]
  (serdes.core/cis->embedded cis->Expression is))

(defn new-Expression
  "Creates a new instance from a map, similar to map->Expression except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::Expression-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::Expression-spec init))))]}
  (-> (merge Expression-defaults init)
      (convert-Expression-expression)
      (map->Expression-record)))

(defn pb->Expression
  "Protobuf to Expression"
  [input]
  (cis->Expression (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record Expression-meta {:type "spyglass.streamline.alpha.ast.Expression" :decoder pb->Expression})

;-----------------------------------------------------------------------------
; Lambda
;-----------------------------------------------------------------------------
(defrecord Lambda-record [inputs body]
  pb/Writer
  (serialize [this os]
    (serdes.complex/write-repeated serdes.core/write-String 1 (:inputs this) os)
    (serdes.core/write-embedded 2 (:body this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.Lambda"))

(s/def :spyglass.streamline.alpha.ast.Lambda/inputs (s/every string?))

(s/def ::Lambda-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.Lambda/inputs ]))
(def Lambda-defaults {:inputs [] })

(defn cis->Lambda
  "CodedInputStream to Lambda"
  [is]
  (->> (tag-map Lambda-defaults
         (fn [tag index]
             (case index
               1 [:inputs (serdes.complex/cis->repeated serdes.core/cis->String is)]
               2 [:body (ecis->Expression is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->Lambda-record)))

(defn ecis->Lambda
  "Embedded CodedInputStream to Lambda"
  [is]
  (serdes.core/cis->embedded cis->Lambda is))

(defn new-Lambda
  "Creates a new instance from a map, similar to map->Lambda except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::Lambda-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::Lambda-spec init))))]}
  (-> (merge Lambda-defaults init)
      (cond-> (some? (get init :body)) (update :body new-Expression))
      (map->Lambda-record)))

(defn pb->Lambda
  "Protobuf to Lambda"
  [input]
  (cis->Lambda (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record Lambda-meta {:type "spyglass.streamline.alpha.ast.Lambda" :decoder pb->Lambda})

;-----------------------------------------------------------------------------
; StreamlineFile
;-----------------------------------------------------------------------------
(defrecord StreamlineFile-record [types modules]
  pb/Writer
  (serialize [this os]
    (serdes.complex/write-repeated serdes.core/write-embedded 1 (:types this) os)
    (serdes.complex/write-repeated serdes.core/write-embedded 2 (:modules this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.StreamlineFile"))

(s/def ::StreamlineFile-spec (s/keys :opt-un []))
(def StreamlineFile-defaults {:types [] :modules [] })

(defn cis->StreamlineFile
  "CodedInputStream to StreamlineFile"
  [is]
  (->> (tag-map StreamlineFile-defaults
         (fn [tag index]
             (case index
               1 [:types (serdes.complex/cis->repeated ecis->TypeDeclaration is)]
               2 [:modules (serdes.complex/cis->repeated ecis->ModuleDef is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->StreamlineFile-record)))

(defn ecis->StreamlineFile
  "Embedded CodedInputStream to StreamlineFile"
  [is]
  (serdes.core/cis->embedded cis->StreamlineFile is))

(defn new-StreamlineFile
  "Creates a new instance from a map, similar to map->StreamlineFile except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::StreamlineFile-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::StreamlineFile-spec init))))]}
  (-> (merge StreamlineFile-defaults init)
      (cond-> (some? (get init :types)) (update :types #(map new-TypeDeclaration %)))
      (cond-> (some? (get init :modules)) (update :modules #(map new-ModuleDef %)))
      (map->StreamlineFile-record)))

(defn pb->StreamlineFile
  "Protobuf to StreamlineFile"
  [input]
  (cis->StreamlineFile (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record StreamlineFile-meta {:type "spyglass.streamline.alpha.ast.StreamlineFile" :decoder pb->StreamlineFile})

;-----------------------------------------------------------------------------
; FunctionCall
;-----------------------------------------------------------------------------
(defrecord FunctionCall-record [identifier arguments]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-String 1  {:optimize true} (:identifier this) os)
    (serdes.complex/write-repeated serdes.core/write-embedded 2 (:arguments this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.FunctionCall"))

(s/def :spyglass.streamline.alpha.ast.FunctionCall/identifier string?)

(s/def ::FunctionCall-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.FunctionCall/identifier ]))
(def FunctionCall-defaults {:identifier "" :arguments [] })

(defn cis->FunctionCall
  "CodedInputStream to FunctionCall"
  [is]
  (->> (tag-map FunctionCall-defaults
         (fn [tag index]
             (case index
               1 [:identifier (serdes.core/cis->String is)]
               2 [:arguments (serdes.complex/cis->repeated ecis->Expression is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->FunctionCall-record)))

(defn ecis->FunctionCall
  "Embedded CodedInputStream to FunctionCall"
  [is]
  (serdes.core/cis->embedded cis->FunctionCall is))

(defn new-FunctionCall
  "Creates a new instance from a map, similar to map->FunctionCall except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::FunctionCall-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::FunctionCall-spec init))))]}
  (-> (merge FunctionCall-defaults init)
      (cond-> (some? (get init :arguments)) (update :arguments #(map new-Expression %)))
      (map->FunctionCall-record)))

(defn pb->FunctionCall
  "Protobuf to FunctionCall"
  [input]
  (cis->FunctionCall (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record FunctionCall-meta {:type "spyglass.streamline.alpha.ast.FunctionCall" :decoder pb->FunctionCall})

;-----------------------------------------------------------------------------
; Function
;-----------------------------------------------------------------------------
(defrecord Function-record [function]
  pb/Writer
  (serialize [this os]
    (write-Function-function  (:function this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.Function"))

(s/def ::Function-spec (s/keys :opt-un []))
(def Function-defaults {})

(defn cis->Function
  "CodedInputStream to Function"
  [is]
  (->> (tag-map Function-defaults
         (fn [tag index]
             (case index
               1 [:function {:lambda (ecis->Lambda is)}]
               2 [:function {:hof (ecis->Hof is)}]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->Function-record)))

(defn ecis->Function
  "Embedded CodedInputStream to Function"
  [is]
  (serdes.core/cis->embedded cis->Function is))

(defn new-Function
  "Creates a new instance from a map, similar to map->Function except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::Function-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::Function-spec init))))]}
  (-> (merge Function-defaults init)
      (convert-Function-function)
      (map->Function-record)))

(defn pb->Function
  "Protobuf to Function"
  [input]
  (cis->Function (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record Function-meta {:type "spyglass.streamline.alpha.ast.Function" :decoder pb->Function})

;-----------------------------------------------------------------------------
; TypeDeclaration
;-----------------------------------------------------------------------------
(defrecord TypeDeclaration-record [name fields]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-String 1  {:optimize true} (:name this) os)
    (serdes.complex/write-repeated serdes.core/write-embedded 2 (:fields this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.TypeDeclaration"))

(s/def :spyglass.streamline.alpha.ast.TypeDeclaration/name string?)

(s/def ::TypeDeclaration-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.TypeDeclaration/name ]))
(def TypeDeclaration-defaults {:name "" :fields [] })

(defn cis->TypeDeclaration
  "CodedInputStream to TypeDeclaration"
  [is]
  (->> (tag-map TypeDeclaration-defaults
         (fn [tag index]
             (case index
               1 [:name (serdes.core/cis->String is)]
               2 [:fields (serdes.complex/cis->repeated ecis->TypeField is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->TypeDeclaration-record)))

(defn ecis->TypeDeclaration
  "Embedded CodedInputStream to TypeDeclaration"
  [is]
  (serdes.core/cis->embedded cis->TypeDeclaration is))

(defn new-TypeDeclaration
  "Creates a new instance from a map, similar to map->TypeDeclaration except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::TypeDeclaration-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::TypeDeclaration-spec init))))]}
  (-> (merge TypeDeclaration-defaults init)
      (cond-> (some? (get init :fields)) (update :fields #(map new-TypeField %)))
      (map->TypeDeclaration-record)))

(defn pb->TypeDeclaration
  "Protobuf to TypeDeclaration"
  [input]
  (cis->TypeDeclaration (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record TypeDeclaration-meta {:type "spyglass.streamline.alpha.ast.TypeDeclaration" :decoder pb->TypeDeclaration})

;-----------------------------------------------------------------------------
; Hof
;-----------------------------------------------------------------------------
(defrecord Hof-record [parent inputs body]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-String 1  {:optimize true} (:parent this) os)
    (serdes.complex/write-repeated serdes.core/write-String 2 (:inputs this) os)
    (serdes.core/write-embedded 3 (:body this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.Hof"))

(s/def :spyglass.streamline.alpha.ast.Hof/parent string?)
(s/def :spyglass.streamline.alpha.ast.Hof/inputs (s/every string?))

(s/def ::Hof-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.Hof/parent :spyglass.streamline.alpha.ast.Hof/inputs ]))
(def Hof-defaults {:parent "" :inputs [] })

(defn cis->Hof
  "CodedInputStream to Hof"
  [is]
  (->> (tag-map Hof-defaults
         (fn [tag index]
             (case index
               1 [:parent (serdes.core/cis->String is)]
               2 [:inputs (serdes.complex/cis->repeated serdes.core/cis->String is)]
               3 [:body (ecis->Expression is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->Hof-record)))

(defn ecis->Hof
  "Embedded CodedInputStream to Hof"
  [is]
  (serdes.core/cis->embedded cis->Hof is))

(defn new-Hof
  "Creates a new instance from a map, similar to map->Hof except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::Hof-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::Hof-spec init))))]}
  (-> (merge Hof-defaults init)
      (cond-> (some? (get init :body)) (update :body new-Expression))
      (map->Hof-record)))

(defn pb->Hof
  "Protobuf to Hof"
  [input]
  (cis->Hof (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record Hof-meta {:type "spyglass.streamline.alpha.ast.Hof" :decoder pb->Hof})

;-----------------------------------------------------------------------------
; ModuleDef
;-----------------------------------------------------------------------------
(defrecord ModuleDef-record [kind identifier signature pipeline]
  pb/Writer
  (serialize [this os]
    (serdes.core/write-String 1  {:optimize true} (:kind this) os)
    (serdes.core/write-String 2  {:optimize true} (:identifier this) os)
    (serdes.core/write-embedded 3 (:signature this) os)
    (serdes.complex/write-repeated serdes.core/write-embedded 5 (:pipeline this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.ModuleDef"))

(s/def :spyglass.streamline.alpha.ast.ModuleDef/kind string?)
(s/def :spyglass.streamline.alpha.ast.ModuleDef/identifier string?)


(s/def ::ModuleDef-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.ModuleDef/kind :spyglass.streamline.alpha.ast.ModuleDef/identifier ]))
(def ModuleDef-defaults {:kind "" :identifier "" :pipeline [] })

(defn cis->ModuleDef
  "CodedInputStream to ModuleDef"
  [is]
  (->> (tag-map ModuleDef-defaults
         (fn [tag index]
             (case index
               1 [:kind (serdes.core/cis->String is)]
               2 [:identifier (serdes.core/cis->String is)]
               3 [:signature (ecis->ModuleSignature is)]
               5 [:pipeline (serdes.complex/cis->repeated ecis->Function is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->ModuleDef-record)))

(defn ecis->ModuleDef
  "Embedded CodedInputStream to ModuleDef"
  [is]
  (serdes.core/cis->embedded cis->ModuleDef is))

(defn new-ModuleDef
  "Creates a new instance from a map, similar to map->ModuleDef except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::ModuleDef-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::ModuleDef-spec init))))]}
  (-> (merge ModuleDef-defaults init)
      (cond-> (some? (get init :signature)) (update :signature new-ModuleSignature))
      (cond-> (some? (get init :pipeline)) (update :pipeline #(map new-Function %)))
      (map->ModuleDef-record)))

(defn pb->ModuleDef
  "Protobuf to ModuleDef"
  [input]
  (cis->ModuleDef (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record ModuleDef-meta {:type "spyglass.streamline.alpha.ast.ModuleDef" :decoder pb->ModuleDef})

;-----------------------------------------------------------------------------
; ModuleSignature
;-----------------------------------------------------------------------------
(defrecord ModuleSignature-record [inputs output]
  pb/Writer
  (serialize [this os]
    (serdes.complex/write-repeated serdes.core/write-String 1 (:inputs this) os)
    (serdes.core/write-String 2  {:optimize true} (:output this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.ModuleSignature"))

(s/def :spyglass.streamline.alpha.ast.ModuleSignature/inputs (s/every string?))
(s/def :spyglass.streamline.alpha.ast.ModuleSignature/output string?)
(s/def ::ModuleSignature-spec (s/keys :opt-un [:spyglass.streamline.alpha.ast.ModuleSignature/inputs :spyglass.streamline.alpha.ast.ModuleSignature/output ]))
(def ModuleSignature-defaults {:inputs [] :output "" })

(defn cis->ModuleSignature
  "CodedInputStream to ModuleSignature"
  [is]
  (->> (tag-map ModuleSignature-defaults
         (fn [tag index]
             (case index
               1 [:inputs (serdes.complex/cis->repeated serdes.core/cis->String is)]
               2 [:output (serdes.core/cis->String is)]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->ModuleSignature-record)))

(defn ecis->ModuleSignature
  "Embedded CodedInputStream to ModuleSignature"
  [is]
  (serdes.core/cis->embedded cis->ModuleSignature is))

(defn new-ModuleSignature
  "Creates a new instance from a map, similar to map->ModuleSignature except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::ModuleSignature-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::ModuleSignature-spec init))))]}
  (-> (merge ModuleSignature-defaults init)
      (map->ModuleSignature-record)))

(defn pb->ModuleSignature
  "Protobuf to ModuleSignature"
  [input]
  (cis->ModuleSignature (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record ModuleSignature-meta {:type "spyglass.streamline.alpha.ast.ModuleSignature" :decoder pb->ModuleSignature})

;-----------------------------------------------------------------------------
; Literal
;-----------------------------------------------------------------------------
(defrecord Literal-record [literal]
  pb/Writer
  (serialize [this os]
    (write-Literal-literal  (:literal this) os))
  pb/TypeReflection
  (gettype [this]
    "spyglass.streamline.alpha.ast.Literal"))

(s/def ::Literal-spec (s/keys :opt-un []))
(def Literal-defaults {})

(defn cis->Literal
  "CodedInputStream to Literal"
  [is]
  (->> (tag-map Literal-defaults
         (fn [tag index]
             (case index
               1 [:literal {:int (serdes.core/cis->Int64 is)}]
               2 [:literal {:str (serdes.core/cis->String is)}]
               3 [:literal {:boolean (serdes.core/cis->Bool is)}]

               [index (serdes.core/cis->undefined tag is)]))
         is)
        (map->Literal-record)))

(defn ecis->Literal
  "Embedded CodedInputStream to Literal"
  [is]
  (serdes.core/cis->embedded cis->Literal is))

(defn new-Literal
  "Creates a new instance from a map, similar to map->Literal except that
  it properly accounts for nested messages, when applicable.
  "
  [init]
  {:pre [(if (s/valid? ::Literal-spec init) true (throw (ex-info "Invalid input" (s/explain-data ::Literal-spec init))))]}
  (-> (merge Literal-defaults init)
      (convert-Literal-literal)
      (map->Literal-record)))

(defn pb->Literal
  "Protobuf to Literal"
  [input]
  (cis->Literal (serdes.stream/new-cis input)))

(def ^:protojure.protobuf.any/record Literal-meta {:type "spyglass.streamline.alpha.ast.Literal" :decoder pb->Literal})

