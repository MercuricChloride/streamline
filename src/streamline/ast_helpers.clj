(ns streamline.ast-helpers)

(defn hof?
  "Returns if a lambda expression is the input to a higher-order fn"
  [input]
  (let [[_ & lambda] input
       [lambda-type] (first lambda)]
       (= lambda-type :parent-function)))

(defn ->function
  "Converts a syntax node into a function ast node.
  (Either) a lambda or a higher order function"
  ([input]
    (if (hof? input)
      (let [[_ & lambda] input
            lambda-body (subvec (vec lambda) 1)
            [_ parent-fn] (first lambda)
            inputs (vec (butlast lambda-body))
            body (last lambda-body)]
        (->function parent-fn inputs body))
      (let [[_ & lambda] input
            inputs (butlast lambda)
            body (second (last lambda))]
        (->function inputs body))))

  ([inputs body] ; NOTE Case where lambda is not the input to a higher-order fn
   {:expression-type "lambda"
    :inputs (vec (map second inputs))
    :body body})

  ([parent-fn inputs body] ; NOTE Case where lambda is the input to a higher-order fn
   {:expression-type "hof"
    :parent parent-fn
    :callback (->function inputs body)}))

(defn ->module-body
  "Returns an AST node for the body of a module"
  [input]
  (into [] (map ->function input)))

(defn ->module-signature
  [input]
  (let [idents (map second input)
        inputs (butlast idents)
        output (last idents)]
    {:inputs (into [] inputs)
     :output output}))

(defn ->map-module
  "Returns an AST node for a module definition"
  [input]
  (let [[type ident signature body] (rest input)
        [_ type] type
        [_ ident] ident
        [_ & signature] signature
        [_ & body] body]
    {:node-type "module-def"
     :type type
     :name ident
     :signature (->module-signature signature)
     :expressions (->module-body body)
     }))
