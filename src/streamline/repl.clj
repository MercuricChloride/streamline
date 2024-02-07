(ns streamline.repl
  (:require
   [nrepl.middleware :refer [set-descriptor!]]
   [nrepl.misc :refer [response-for]]
   [nrepl.server :refer [start-server]]
   [nrepl.transport :as t]
   [streamline.ast.parser :refer [parser streamline->clj]])
  (:gen-class))

(def nrepl-port 7869)

        ;(doall (map #(t/send transport {:op "eval" :code (pr-str %)}) converted-code))

(defn streamline-eval
  [{:keys [op ^t/Transport transport] :as msg} handler]
  (let [code (:code msg) ; Get the code to evaluate from the message
        converted-code (streamline->clj code) ; Convert your language syntax to Clojure
        _ (println "Streamline Eval")]
    (handler (assoc msg :op "eval" :code converted-code))
    (t/send transport (response-for msg
                                    :status :done
                                    :expansion (pr-str converted-code)))))

(defn custom-eval-middleware
  [handler]
  (fn [{:keys [op] :as msg}]
    (if (= "streamline-eval" op)
      (streamline-eval msg handler)
      (handler msg))))

(set-descriptor! #'custom-eval-middleware
                 {:requires #{"session"}
                  :expects #{"eval"}
                  :handles {"streamline-eval"
                            {:doc "Get macroexpanded code"
                             :requires {"code" "streamline code to eval"}
                             :optional {}
                             :returns {"expansion" "The expanded clojure code to eval"}}}})

;; (defn handler [request]
;;   (let [src (str (get-in request [:body "src"]))
;;         clj (streamline->clj src)]
;;     (response/response clj)))

;; (defroutes app
;;   (GET "/" [] "Hello World")
;;   (POST "/nrepl" [] (wrap-json-body handler))
;;   (route/not-found "Not Found"))

(defn repl-init
  "Starts up the nrepl server, as well as the api to interact with the server"
  ([]
   (repl-init nrepl-port))
  ([port]
   (start-server :port port :handler (nrepl.server/default-handler #'custom-eval-middleware))
   (println "SERVER STARTED!")))

;(stop-server :port nrepl-port)
;(start-server :port nrepl-port)
(def test-code "
mfn foo = a
    |> map (a) => inc(a);

mfn bar = a
    |> (a) => foo(a);
    |> map (item) => inc(item);
")
(parser test-code)

(def to-clj (streamline->clj test-code))
(map eval to-clj)
(foo '(1 2 3))
