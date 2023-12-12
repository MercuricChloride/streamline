(ns streamline.macros)

(defmacro match
  [val & cases]
  (let [cases (mapcat (fn [[case expr]]
                     `((= ~val ~case) ~expr)) (partition 2 cases))]
    `(cond
       ~@cases)))

(match 6
  5 "It's 5"
  6 "This isn't right....")
