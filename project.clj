(defproject streamline "0.1.0"
  :description "A declarative language for building substreams"
  :url "http://spygpc.com"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :plugins [[io.taylorwood/lein-native-image "0.3.1"]]
  :native-image {:name "streamline"                 ;; name of output image, optional
                 ;:graal-bin "/path/to/graalvm/" ;; path to GraalVM home, optional
                 :opts ["--verbose"]}           ;; pass-thru args to GraalVM native-image, optional
  :main streamline.core
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [instaparse "1.4.12"]
                 [io.github.protojure/core "2.0.1"]
                 [io.github.protojure/google.protobuf "2.0.0"]
                 [rm-hull/infix "0.4.0"]
                 [org.clojure/data.json "2.4.0"]
                 [camel-snake-kebab "0.4.3"]
                 [pogonos "0.2.1"]
                 [clj-commons/clj-yaml "1.0.27"]
                 [compojure "1.7.0"]
                 [http-kit "2.7.0"]
                 [ring/ring-json "0.5.1"]
                 [nrepl "1.1.0"]]
  :profiles {:uberjar {:aot :all}})
