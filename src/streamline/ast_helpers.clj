(ns streamline.ast-helpers)

(defn second-or-rest
  "Returns the second element of a vector and the rest of the vector"
  [input]
  (cond
    (empty? input) nil
    (and (= 2 (count input)) (not= (first input) :module-body)) (second input)
    :else (into [] (rest input))))

(defn ho-fn?
  "Returns if a lambda expression is the input to a higher-order fn"
  [input]
  (= (first (second input)) :parent-function))

(defn ->lambda
  "Converts a syntax node into a lambda expression"
  ([input]
    (if (ho-fn? input)
      (let [[_ & lambda] input
            lambda-body (subvec (vec lambda) 1)
            [_ parent-fn] (first lambda)
            inputs (vec (butlast lambda-body))
            body (last lambda-body)]
        (->lambda parent-fn inputs body))
      (let [[_ & lambda] input
            inputs (butlast lambda)
            body (second (last lambda))]
        (->lambda inputs body))))

  ([inputs body]
   {:expression-type "lambda"
    :inputs (vec (map second inputs))
    :expression body})

  ([parent-fn inputs body]
   {:expression-type "ho-fn"
    :parent-function parent-fn
    :lambda (->lambda inputs body)}))

(defn ->module-body
  "Returns an AST node for the body of a module"
  [input]
  (into [] (map ->lambda input)))

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
  (let [[type ident signature body] (map second-or-rest (rest input))]
    {:node-type "module-def"
     :type type
     :name ident
     :signature (->module-signature signature)
     :expressions (->module-body body)}))
