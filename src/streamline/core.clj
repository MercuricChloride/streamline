(ns streamline.core
  (:require
   [streamline.ast.parser :refer [try-parse]]
   [streamline.bundle :refer [bundle-file]]
   [streamline.templating.rust.helpers :refer [use-statements]]
   [compojure.core :refer [defroutes GET]]
   [compojure.route :as route]
   [org.httpkit.server :refer [run-server]])
  (:gen-class))

(defroutes app
  (GET "/" [] "Hello World")
  (route/not-found "Not Found"))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (run-server app {:port 8080})
  (println "Running server on port 8080"))
