(ns streamline.core
  (:require
   [streamline.repl :as repl])
  (:gen-class))

(def test-code "
stream asdf;
mfn foo = a
    |> (a) => a * 2;

mfn asdf = a
    |> (a) => a * 2;
")

(defn -main
  "Startup the streamline api"
  [& args]
  (repl/repl-init 12345)
  ;(repl/startup-nrepl test-code 12345)
  )
