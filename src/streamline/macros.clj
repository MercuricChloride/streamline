(ns streamline.macros)

(defmacro with-attributes
  "Transforms the streamline code, into valid clojure code, using context from the attributes"
  [_attributes & forms]
  `(do
     ~@forms))

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
