(ns streamline.ast.parser
  (:require
   [clojure.edn :as edn]
   [clojure.pprint :as pprint]
   [instaparse.core :as insta :refer [defparser]]))

(def parser
  (insta/parser
   "<S> = file-meta import-statement* (module /interface-def / fn-def / contract-instance )*

    file-meta = file-type identifier <';'>
    <file-type> = 'stream' / 'sink'

    import-statement = (normal-import / import-as)
    normal-import = <'import'> string <';'>
    import-as = <'import'> string <'as'> identifier <';'>

    lambda = <'('> fn-args <')'> <'=>'> (expression <';'>)
    hof = parent-function lambda
    fn-args = identifier*
    <parent-function> = ('filter' / 'map' / 'reduce' / 'apply')
    pipeline = (pipe (lambda / hof))*
    pipe = '|>'

    <expression> = (literal /  function-call / binary-expression / field-access / identifier)

    literal = (address / number / string / boolean / array-literal)

    binary-expression = expression binary-op expression
    binary-op = ('+' / '-' / '*' / '/' / '==' / '!=' / '<' / '>' / '<=' / '>=' / '&&' / '||' / '!')

    array-literal = <'['> expression* <']'>

    function-call = identifier <'('> expression* <')'>

    field-access = expression (<'.'> identifier)+

    module = module-type identifier <'='> module-inputs pipeline
    <module-type> = 'mfn' | 'sfn'
    <store-update-policy> = ('Set' / 'SetNotExists' / 'Add' / 'Min' / 'Max') <'('> (type) <')'>
    module-inputs = ( multi-input / single-input )
    single-input = identifier
    multi-input = <'['> identifier+ <']'>

    fn-def = <'fn'> identifier <':'> fn-signature <'{'> pipeline <'}'>
    fn-signature = <'('> fn-inputs <')'> <'->'> type
    fn-inputs = type*

    contract-instance = type identifier <'='> type <'('> address <')'> <';'>

    struct-def = <'struct'> identifier <'{'> (struct-field <';'>)* <'}'>
    struct-field = type identifier ('[' ']')?

    interface-def = <'interface'> identifier <'{'> ((event-def / function-def) <';'>)* <'}'>

    event-def = attributes <'event'> identifier <'('> event-param* <')'>
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

    attributes = (<'#['> key-value* <']'> / '')
    key-value = key <'='> value
    <key> = 'mfn' / 'addresses'
    value = literal

    <identifier> = #'[a-zA-Z$_][a-zA-Z0-9$_]*'
    fully-qualified-identifier = identifier (<'.'> identifier)+
    event-array = identifier <'.'> identifier '[]'
    <type> = ( type '[]' / fully-qualified-identifier / identifier)
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
      (insta/transform
       expr-transform-map
       output))))

(def test-code "
stream foo;

interface Erc721 {
    #[mfn = \"erc721_transfers\",
      addresses = [0xbc4ca0eda7647a8ab7c2061c2e118a18a936f13d]]
    event Transfer(address indexed from, address indexed to, uint256 indexed tokenId);
    #[mfn = \"erc721_approvals\"]
    event Approval(address indexed owner, address indexed approved, uint256 indexed tokenId);
    #[mfn = \"erc721_approval_for_alls\"]
    event ApprovalForAll(address indexed owner, address indexed operator, bool approved);
}

mfn burns = erc721_transfers
|> filter (transfer) => transfer.address == 69;
")


(parser test-code)
