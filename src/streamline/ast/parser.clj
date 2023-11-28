(ns streamline.ast.parser
  (:require
   [clojure.edn :as edn]
   [clojure.pprint :as pprint]
   [clojure.string :as string]
   [instaparse.core :as insta]))

(def parser
  (insta/parser
   "<S> = file-meta import-statement* (module / struct-def / interface-def / fn-def / contract-instance / conversion)*

    file-meta = file-type identifier <';'>
    <file-type> = 'stream' / 'sink'

    import-statement = (normal-import / import-as)
    <normal-import> = <'import'> string <';'>
    <import-as> = <'import'> string <'as'> identifier <';'>

    <anon-fn> = <'('> fn-args <')'> <'=>'> (expression <';'>)
    lambda = anon-fn
    callback = anon-fn
    hof = parent-function callback
    fn-args = identifier*
    <parent-function> = ('filter' / 'map' / 'reduce' / 'apply')
    pipeline = (lambda / hof)*

    conversion = <'convert:'> type <'->'> type <'{'> pipeline <'}'>

    <expression> = (number / string / address/ struct-expression / array-expression / convert / function-call / binary-expression / field-access / expr-ident)
    expr-ident = identifier

    struct-expression = identifier <'{'> struct-expression-field* <'}'>
    struct-expression-field = identifier <':'> expression <','>?

    array-expression = <'['> expression* <']'>

    convert = <'convert'> <'('> identifier type <')'>
    function-call = identifier <'('> expression* <')'>

    binary-op = ('+' / '-' / '*' / '/' / '==' / '!=' / '<' / '>' / '<=' / '>=' / '&&' / '||' / '!')
    binary-expression = expression binary-op expression

    field-access = expression (<'.'> identifier)+

    module = module-type identifier <':'> module-signature <'{'> pipeline  <'}'>
    <module-type> = 'mfn' | 'sfn'
    module-signature = map-module-signature / store-module-signature
    <map-module-signature> = <'('> module-inputs <')'> <'->'> module-output
    <store-module-signature> = <'('> module-inputs <')'> <'->'> store-update-policy
    <store-update-policy> = ('Set' / 'SetNotExists' / 'Add' / 'Min' / 'Max') <'('> (type) <')'>
    module-inputs = ( event-array / chained-module )*
    chained-module = identifier
    module-output = type

    fn-def = <'fn'> identifier <':'> fn-signature <'{'> pipeline <'}'>
    fn-signature = <'('> fn-inputs <')'> <'->'> type
    fn-inputs = type*

    contract-instance = type identifier <'='> type <'('> address <')'> <';'>

    struct-def = <'struct'> identifier <'{'> (struct-field <';'>)* <'}'>
    struct-field = type identifier ('[' ']')?

    interface-def = <'interface'> identifier <'{'> ((event-def / function-def) <';'>)* <'}'>

    event-def = <'event'> identifier <'('> event-param* <')'>
    <event-param> = (indexed-event-param / non-indexed-event-param)
    indexed-event-param = type <'indexed'> identifier
    non-indexed-event-param = type identifier

    <function-def> = (function-w-return / function-wo-return)
    function-w-return = <'function'> identifier <'('> function-params <')'> <function-modifier*> returns
    function-wo-return = <'function'> identifier <'('> function-params <')'> <function-modifier*>
    function-params = function-param*
    function-param = type <location?> identifier
    location = 'memory' / 'storage' / 'calldata'
    function-modifier = visibility / mutability
    visibility = 'public' / 'private' / 'internal' / 'external'
    mutability = 'view' / 'pure'
    returns = <'returns'> <'('> return-param* <')'>
    <return-param> = (named-return / unnamed-return)
    unnamed-return = type <location?>
    named-return = type <location?> identifier

    <identifier> = #'[a-zA-Z$_][a-zA-Z0-9$_]*'
    <array-identifier> = #'[a-zA-Z$_][a-zA-Z0-9$_]*' '[]'
    <fully-qualified-identifier> = identifier (<'.'> (array-identifier / identifier))+
    event-array = identifier <'.'> identifier '[]'
    type = (fully-qualified-identifier / array-identifier / identifier)
    number = #'[0-9]+'
    string = <'\"'> #'[^\"\\n]*' <'\"'>
    boolean = 'true' / 'false'
    array = <'['> <']'>
    address = #'^0x[a-fA-F0-9]{40}'
    "
   :auto-whitespace :comma))

(def expr-transform-map
  {:number edn/read-string
   :string edn/read-string
   :boolean edn/read-string
   :binary-op edn/read-string
   :expr-ident edn/read-string
   ;:binary-expression str
   ;; :binary-expression (fn [left op right]
   ;;                      [op left right])
   ;; :function-call (fn [name & args]
   ;;                  [(symbol name) args])
   })

(defn try-parse
  [path]
  (let [output (parser (slurp path))]
    (if-let [failure (insta/get-failure output)]
      (do
        (println "FAILED TO PARSE STREAMLINE FILE: " path "\n\n\n")
        (pprint/pprint failure)
        (println "\n\n\n")
        (throw (Exception. (str "Failed to parse streamline file: " path "\n" failure))))
      (insta/transform
       expr-transform-map
       output)
      )))
