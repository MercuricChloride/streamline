(ns streamline.core
  (:require
   [streamline.repl :as repl])
  (:gen-class))

(defn -main
  "Startup the streamline api"
  [& args]
  (repl/repl-init))
