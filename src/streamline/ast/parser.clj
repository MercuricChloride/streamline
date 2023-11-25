(ns streamline.ast.parser
  (:require [instaparse.core :as insta]))

(def parser
  (insta/parser
   "<S> = file-meta import-statement* (module / struct-def / interface-def / fn-def / contract-instance / conversion)*

    file-meta = file-type identifier <';'>
    <file-type> = 'stream' / 'sink'

    import-statement = (normal-import / import-as)
    <normal-import> = <'import'> string <';'>
    <import-as> = <'import'> string <'as'> identifier <';'>

    lambda = <'('> module-inputs <')'> <'=>'> ( (<'{'> (expression <';'>)* <'}'>) / (expression <';'>) )
    hof = parent-function <'('> module-inputs <')'> <'=>'> ( (<'{'> (expression <';'>)* <'}'>) / (expression <';'>) )
    <parent-function> = ('filter' / 'map' / 'reduce' / 'apply')
    pipeline = (lambda / hof)*

    conversion = <'convert:'> type <'->'> type <'{'> pipeline <'}'>

    <expression> = (number / string / address/ struct-expression / array-expression / function-call / binary-expression / field-access / identifier)

    struct-expression = identifier <'{'> struct-expression-field* <'}'>
    struct-expression-field = identifier <':'> expression <','>?

    array-expression = <'['> expression* <']'>

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
    module-inputs = ( event-array / identifier)*
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
