(ns streamline.repl)

(defmulti eval-expr :expression-type)

(defmethod eval-expr :identifier
    [input]
    (eval (symbol (:ident input))))

(defmethod eval-expr :number
    [input]
    (Integer/parseInt (:number input)))

(defmethod eval-expr :function-call
    [input]
    (let [function-name (:function-name input)
          args (:args input)]
        (apply (eval-expr function-name) (map eval-expr args))))

(defmacro step->
  "Like as->, but only performs the first COUNT forms"
  [expr count & forms]
  (let [forms (take count forms)]
    `(->> ~expr ~@forms)))

(step-> [1 2 3 4] 1
        (map inc)
        (filter even?)
        (map inc))

;; (defn module?
;;   "Tests if an AST node is a module-def"
;;   [node]
;;   (= (first node) :module))


;; (def modules (map ->map-module (rest ast)))

;; (let [[_ type ident sig body] (first (rest ast))
;;       [_ & lambdas] body]
;;   (map hof? lambdas))

;(->map-module (first  (rest ast)))
