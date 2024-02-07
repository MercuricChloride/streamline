(ns streamline.ast.parser
  (:require
   [clojure.edn :as edn]
   [clojure.pprint :as pprint]
   [instaparse.core :as insta]))

(def parser
  (insta/parser
   "<S> = file-meta? import-statement* top-level-interaction*

    file-meta = file-type identifier <';'>
    <file-type> = 'stream' / 'sink'

    top-level-interaction = instance-def / module-def

    (* ================= *)
    (* IMPORT STATEMENTS *)
    (* ================= *)
    import-statement = (normal-import / import-as)
    normal-import = <'import'> string <';'>
    import-as = <'import'> string <'as'> identifier <';'>

    (* ================= *)
    (* MODULE THINGS     *)
    (* ================= *)
    module-def = attributes ( mfn-def / sfn-def / fn-def )

    mfn-def = <'mfn'> identifier <'='> module-inputs pipeline
    sfn-def = <'sfn'> identifier <'='> module-inputs pipeline
    fn-def = <'fn'> identifier <'='> module-inputs pipeline

    module-inputs = ( multi-input / single-input )
    store-deltas = identifier <'.'> <'deltas'>
    single-input = ( store-deltas / identifier)
    multi-input = <'['> single-input+ <']'>

    (* =================  *)
    (* CONTRACT INSTANCES *)
    (* =================  *)
    instance-def = identifier <'='> identifier <'('> address <')'> <';'>

    (* =================  *)
    (* FUNCTION THINGS    *)
    (* =================  *)
    lambda = <'('> fn-args <')'> <'=>'> expression <';'>
    hof = parent-function lambda
    fn-args = identifier*
    parent-function = ('filter' / 'map' / 'reduce' / 'apply')
    pipeline = (pipe (hof / lambda))*
    <pipe> = <'|>'>

    (* =================  *)
    (* EXPRESSIONS        *)
    (* =================  *)
    <expression> = do-block / literal / rpc-call /function-call / var-assignment / binary-expression / field-access / identifier

    do-block = <'do'> <'{'> (expression <';'>)* <'}'>
    <literal> = (map-literal / list-literal / tuple-literal / address / number / string / boolean)

    var-assignment = identifier <'='> expression (* used to assign local mutable *)

    store-interaction = store-set / store-get / store-delete
    store-get = 'get' <'('> identifier expression <')'> (* get(ident, key) *)
    store-set = 'set' <'('> expression expression <')'> (* set(key, value) *)
    store-delete = 'delete' <'('> expression <')'> (* delete(key-prefix) *)

    binary-expression = expression binary-op expression (* 1 + 2 *)
    binary-op = ('+' / '-' / '*' / '/' / '==' / '!=' / '<' / '>' / '<=' / '>=' / '&&' / '||')

    function-call = identifier <'('> expression* <')'>
    (* RPC CALLS ARE OF THE FORM: *)
    (* INSTANCE.FN-NAME(ARGS...) *)
    rpc-call = <'#'> identifier <'.'> identifier <'('> expression* <')'> <'#'>

    (* NOTE This is an ident or number because we use field access for list indexing *)
    field-access = expression (<'.'> (identifier / number))+

    (* =================  *)
    (* ATTRIBUTES         *)
    (* =================  *)
    attributes = attribute*
    attribute = kv-attribute / value-attribute / tag-attribute
    tag-attribute = '@' identifier
    kv-attribute = '@' identifier identifier <'='> expression
    value-attribute = '@' identifier <'='> expression

    (* =================  *)
    (* LITERALS           *)
    (* =================  *)
    list-literal = <'['> expression* <']'>
    tuple-literal = <'('> expression* <')'>
    map-literal = <'{'> (identifier <':'> expression)* <'}'>
    identifier = #'[a-zA-Z$_][a-zA-Z0-9$_]*'
    number = #'[0-9]+'
    string = <'\"'> #'[^\"\\n]*' <'\"'>
    boolean = 'true' / 'false'
    address = #'^0x[a-fA-F0-9]{40}'
    "
   :auto-whitespace :comma))

(def expr-transform-map
  {:number edn/read-string
   :string edn/read-string
   :boolean edn/read-string
   :binary-op edn/read-string
   :expr-ident edn/read-string})

(defn try-parse
  [path]
  (let [output (parser (slurp path))]
    (if-let [failure (insta/get-failure output)]
      (do
        (println "FAILED TO PARSE STREAMLINE FILE: " path "\n\n\n")
        (pprint/pprint failure)
        (println "\n\n\n")
        (throw (Exception. (str "Failed to parse streamline file: " path "\n" failure))))
      ;; (insta/transform
      ;;  expr-transform-map
      ;;  output)
      output)))

;; (def test-code "
;; stream foo;
;; import \"ERC20.sol\";
;; import \"interfaces/Blur.sol\";

;; bayc = ERC721(0xBC4CA0EdA7647A8aB7C2061c2E118A18a936f13D);
;; milady = ERC721(0x5Af0D9827E0c53E4799BB226655A1de152A425a5);
;; blur = Blur(0x000000000000Ad05Ccc4F10045630fb830B95127);
;; azuki = ERC721(0xED5AF388653567Af2F388E6224dC7C4b3241C544);

;; mfn miladyTransfers = EVENTS
;;     |> (events) => events.milady.Transfer;
;;     |> map (t) => { foo: 123 };

;; mfn smolBayc = EVENTS
;;     |> (events) => events.bayc.Transfer;
;;     |> map (t) => t._tokenId;

;; mfn baycTransfers = EVENTS
;;     |> (events) => events.bayc.Transfer;
;;     |> map (transfer) => {
;;        epicToken: transfer._tokenId,
;;        greeting: \"Hello!\",
;;        advancedGreeting: \"Hello\" + \" World!\",
;;        stringComp: \"Hello\" > \"World!\",
;;        num: 42 + 15 + transfer._tokenId,
;;        numberComp: 42 > 10,
;;        divTest: 42 / 15,
;;        mulTest: 42 * 15,
;;        subTest: 42 - 15,
;;        compareTest: 42 == 42,
;;        foo: true,
;;        addr: 0xBC4CA0EdA7647A8aB7C2061c2E118A18a936f13D,
;;        addrCompare: 0x5Af0D9827E0c53E4799BB226655A1de152A425a5 == 0xBC4CA0EdA7647A8aB7C2061c2E118A18a936f13D,
;;        baycEqual: 0xBC4CA0EdA7647A8aB7C2061c2E118A18a936f13D == 0xBC4CA0EdA7647A8aB7C2061c2E118A18a936f13D,
;;        tuple: (1, 2, 3),
;;        list: [1, 2, [3,4,[5,6]]],
;;        listAccess: [1, 2, 3].0,
;;        doBlock: do{
;;            123 + 123;
;;            69
;;        },
;;        rpcTest: #milady.ownerOf(transfer._tokenId)#
;;     };

;; mfn blurTrades = EVENTS
;;     |> (events) => events.blur.OrdersMatched;

;; mfn comp = [miladyTransfers, smolBayc]
;;     |> (milady, smol) => {
;;        milady: milady,
;;        smol: smol
;;     };

;; mfn helloJordan = [EVENTS, baycTransfers]
;;     |> (events, transfers) => events.blur.OrdersMatched;
;;     |> map (order) => {
;;        maker: order.maker,
;;        taker: order.taker,
;;        epic: order.sellHash
;;     };

;; @immutable
;; sfn storeAzukiOwners = EVENTS
;;     |> (events) => events.azuki.Transfer;
;;     |> map (t) => set(t._tokenId, t._to);

;; mfn mapAzukiOwnerChanges = storeAzukiOwners.deltas
;;     |> (deltas) => deltas;

;; mfn azukiTransfers = EVENTS
;;     |> (events) => events.azuki.Transfer;
;; ")

(def test-code "
stream foo;
import \"ERC20.sol\";

milady = ERC721(0x5Af0D9827E0c53E4799BB226655A1de152A425a5);

mfn miladyTransfers = EVENTS
    |> (events) => events.milady.Transfer;
    |> map (t) => { foo: 123 };
")

(def modules (atom #{}))

(def module-edges (atom []))

(defn store-inputs
  [inputs module-name]
  (for [input inputs]
    (swap! module-edges conj {:from input :to module-name})))

(defmacro with-attributes
  "Transforms the streamline code, into valid clojure code, using context from the attributes"
  [_attributes & forms]
  `(do
     ~@forms))

(defmulti ->clj first)

(defmacro generate-clj
  {:clj-kondo/lint-as 'clojure.core/defn}
  [fields]
  `(vec ~(map #(list (symbol %) (->clj %)) (list* fields))))

(defmacro defclj
  "Like defmethod, but applies ->clj to all of the module, as well as removes the first hiccup identifier"
  {:clj-kondo/ignore [:unresolved-symbol]}
  ([case fields]
   (let [formatted-inputs# (mapcat #(list '->clj %) fields)]
     `(defmethod ->clj ~case
        [~(vec (list* '_ fields))]
        ~formatted-inputs#)))
  ([case fields body]
   (let [formatted-inputs# (vec (mapcat #(list (symbol %) (list '->clj %)) fields))]
     `(defmethod ->clj ~case
        [~(vec (list* '_ fields))]
        (let ~formatted-inputs#
         ~body)))))


(defmacro defclj*
  "Like defclj, but doesn't apply ->clj to it's args"
  {:clj-kondo/lint-as 'clojure.core/defn}
  ([case fields body]
   `(defmethod ->clj ~case
         [~(vec (list* '_ fields))]
         ~body)))

;; NOTE This is a dynamic variable that tells us if we are in a higher order function or not
;; This dictates whether or not to generate a lambda with the apply keyword, or just the lambda
(def ^:dynamic in-hof? false)

(defmethod ->clj :default
  [_]
  nil)

(defclj :top-level-interaction
  [interaction])

(defclj :module-inputs
  [input])

(defclj :single-input
  [input]
  [input])

(defmethod ->clj :multi-input
  [[_ & inputs]]
  (vec (mapcat ->clj inputs)))

(defclj* :identifier
  [ident]
  (symbol ident))

(defclj :module-def
  [attributes module]
  (with-attributes attributes module))

(defclj :mfn-def
  [name inputs pipeline]
  `(defn ~name
     [~@inputs]
     (->> (list ~@inputs)
      ~@pipeline)))

(defclj :sfn-def
  [name inputs pipeline]
  `(defn ~name
     [~@inputs]
     (->> (list ~@inputs)
      ~@pipeline)))

(defclj :fn-def
  [name inputs pipeline]
  `(defn ~name
     [~@inputs]
     (->> (list ~@inputs)
        ~@pipeline)))

(defmethod ->clj :pipeline
  [[_ & applications]]
  (map ->clj applications))

;; (defclj :pipeline
;;   [pipeline])

;; (defclj :lambda
;;   [args expression]
;;   `(apply (fn [~@args] ~expression)))

(defclj :lambda
  [args expression]
  (if in-hof?
    `(fn [~@args] ~expression)
    `(apply (fn [~@args] ~expression))))

(defclj* :hof
  [kind lambda]
  (binding [in-hof? true]
    (let [kind (->clj kind)
          lambda (->clj lambda)]
        `(~kind ~lambda))))

(defclj* :parent-function
  [kind]
  (symbol kind))

(defmethod ->clj :fn-args
  [[_ & args]]
  (map ->clj args))

(defmethod ->clj :list-literal
  [[_ & items]]
  `(list ~@(map ->clj items)))
(defmethod ->clj :list-literal
  [[_ & items]]
  `(list ~@(map ->clj items)))


(defclj* :number
  [num]
  (edn/read-string num))

(defclj* :string
  [string]
  (str string))

(defclj* :boolean
  [bool]
  (edn/read-string bool))

(defclj* :binary-op
  [op]
  (cond
    (= "||" op) `or
    (= "&&" op) `and
    :else (symbol op)))

(defclj :binary-expression
  [lh op rh]
  `(~op ~lh ~rh))

(defclj :function-call
  [function args]
  `(apply ~function (list ~args)))

(defmethod ->clj :do-block
  [[_ & exprs]]
  (let [exprs (map ->clj exprs)]
    `(do ~@exprs)))


(defn transform-module
  [kind ident inputs pipeline]
  (swap! modules conj ident)
  (store-inputs inputs ident)
  (let [input-symbol (gensym "inputs")]
    `(defn ~(symbol ident)
       [~@inputs]
       (let [~input-symbol ~@inputs]
         (->> ~input-symbol
              ~@pipeline)))))

(defn transform-event-def
  [{:keys [:mfn :addresses]} & _]
  (swap! modules conj mfn)
  `(defn ~(symbol mfn)
     []))

(defn transform-attributes
  [& attributes]
  (reduce merge attributes))

(defn transform-kv
  [key value]
  {(keyword key) value})

(defn transform-function-call
  [function args]
  `(~(symbol function) ~@args))

(defn transform-field-access
  [obj & fields]
  (let [obj (symbol obj)
        field-keys (map keyword fields)]
    `(-> ~obj
         ~@field-keys)))

(defn transform-binary-expr
  [lh op rh]
  `(~op ~lh ~rh))

(defn transform-pipeline
  [_ & funcs]
  funcs)

(defn transform-hof
  [kind lambda]
  `(~(symbol kind) ~lambda))

(defn transform-lambda
  [args body]
  `(fn [~@args]
     ~body))

(defn transform-fn-args
  [& args]
  (map symbol args))

(defn transform-multi-input
  [& inputs]
  (vector (map symbol inputs)))

(def binary-op-map {"==" '==
                    "!=" 'not=
                    "+" '+
                    "-" '-
                    "*" '*
                    "/" '/
                    "<" '<
                    ">" '>
                    "<=" '<=
                    ">=" '>=
                    "&&" 'and
                    "||" 'or
                    "!" 'not})

(defn transform-binary-op
  [op]
  (get binary-op-map op))

(defn transform-file-meta
  [_kind ident]
  `(ns ~(symbol ident)))

(defn transform-interface-def
  [name & rest]
  `((ns ~(symbol name))
    ~@rest))

(def repl-transform-map
  {:number edn/read-string
   :boolean edn/read-string
   :module transform-module
   :event-def transform-event-def
   :interface-def transform-interface-def
   :file-meta transform-file-meta

   :value identity
   :string identity
   :address identity
   :single-input symbol
   :binary-op transform-binary-op
   :multi-input transform-multi-input

   :module-inputs vector
   :array-literal vector
   :key-value transform-kv
   :attributes transform-attributes
   :literal identity
   :function-call transform-function-call
   :field-access transform-field-access
   :binary-expression transform-binary-expr

   :pipeline transform-pipeline
   :fn-args transform-fn-args
   :lambda transform-lambda
   :hof transform-hof})

(defn streamline->clj
  [source]
  (as-> source t
       (parser t)
       (map ->clj t)
       (filter #(not (nil? %)) t)
       (doall t)
       t))
