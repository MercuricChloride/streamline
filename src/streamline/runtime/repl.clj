(ns streamline.runtime.repl
  (:require
   [nrepl.middleware :refer [set-descriptor!]]
   [nrepl.server :refer [start-server]]
   [streamline.ast.interpreter :refer [streamline->clj]])
  (:gen-class))

(def nrepl-port 7869)

;; NOTE I might want to add a custom response-for function that will provide some additional information about the evaluation for debugging purposes
(defn streamline-eval
  [{:keys [code] :as msg} handler]
  (let [converted-code (streamline->clj code)] ; Convert your language syntax to Clojure
    (handler (assoc msg :op "streamline-eval" :code converted-code))))

(defn streamline-eval-middleware
  [handler]
  (fn [{:keys [op] :as msg}]
    (if (= "streamline-eval" op)
      (streamline-eval msg handler)
      (handler msg))))

(set-descriptor! #'streamline-eval-middleware
                 {:requires #{"session"}
                  :expects #{"eval"}
                  :handles {"streamline-eval"
                            {:doc "Evalutes streamline code as clojure code"
                             :requires {"code" "streamline code to eval"}
                             :optional {}
                             :returns {"expansion" "The expanded clojure code to eval"}}}})

(def -middleware-list
  (list streamline-eval-middleware))

(defn repl-init
  "Starts up the nrepl server, as well as the api to interact with the server"
  ([]
   (repl-init nrepl-port))
  ([port]
   (start-server
    :port port
    :handler (nrepl.server/default-handler #'streamline-eval-middleware))
   (println "SERVER STARTED!")))
