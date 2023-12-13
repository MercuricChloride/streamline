(ns streamline.repl
  (:require
   [compojure.core :refer [defroutes GET POST]]
   [compojure.route :as route]
   [nrepl.core :as nrepl]
   [nrepl.server :refer [start-server]]
   [org.httpkit.server :refer [run-server]]
   [ring.middleware.json :refer [wrap-json-body]]
   [ring.util.response :as response]
   [streamline.ast.parser :refer [streamline->clj]])
  (:gen-class))

(def nrepl-port 7869)

(defn startup-nrepl
  [code]
  (with-open [conn (nrepl/connect :port nrepl-port)]
    (-> (nrepl/client conn 1000)
        (nrepl/message {:op "eval" :code (str code)})
        nrepl/response-values
        str)))

(defn handler [request]
  (let [src (str (get-in request [:body "src"]))
        clj (streamline->clj src)]
    (response/response clj)))

(defroutes app
  (GET "/" [] "Hello World")
  (POST "/nrepl" [] (wrap-json-body handler))
  (route/not-found "Not Found"))

(defn repl-init
  "Starts up the nrepl server, as well as the api to interact with the server"
  []
  (start-server :port nrepl-port)
  (run-server app {:port 8080})
  (println "Running server on port 8080"))
