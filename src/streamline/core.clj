(ns streamline.core
  (:require
   [compojure.core :refer [defroutes GET]]
   [compojure.route :as route]
   [nrepl.core :as nrepl]
   [nrepl.server :refer [start-server]]
   [org.httpkit.server :refer [run-server]])
  (:gen-class))

(def nrepl-port 7869)

(defn test-nrepl
  []
  (with-open [conn (nrepl/connect :port nrepl-port)]
    (-> (nrepl/client conn 1000)
        (nrepl/message {:op "eval" :code "(+ 1 2 3)"})
        nrepl/response-values
        str)))

(defroutes app
  (GET "/" [] "Hello World")
  (GET "/nrepl" [] (test-nrepl))
  (route/not-found "Not Found"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (start-server :port nrepl-port)
  (run-server app {:port 8080})
  (println "Running server on port 8080"))
