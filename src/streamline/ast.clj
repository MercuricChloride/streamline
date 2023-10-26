(ns streamline.ast)

(defn second-or-rest
  "Returns the second element of a vector and the rest of the vector"
  [input]
  (cond
    (empty? input) nil
    (= 2 (count input)) (second input)
    :else (into [] (rest input))))

(defn ->lambda
  [input]
  (let [[_ & lambda] input
        inputs (butlast lambda)
        [_ expression] (last lambda)]
    {:expression-type "lambda"
     :inputs (into [] (map #(second %) inputs))
     :expression expression}))

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
     :module-type type
     :module-name ident
     :module-signature (->module-signature signature)
     :expressions (->module-body body)}))
