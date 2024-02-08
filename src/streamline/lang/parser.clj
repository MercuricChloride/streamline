(ns streamline.lang.parser
  (:require
   [instaparse.core :as insta]))

(def parser
  (insta/parser
   "<S> = file-meta? import-statement* top-level-interaction*

    file-meta = file-type identifier <';'>
    <file-type> = 'stream' / 'sink'

    top-level-interaction = instance-def / module-def / expression

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
