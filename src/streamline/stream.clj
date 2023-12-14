(ns streamline.stream
  (:require [protojure.grpc.client.providers.http2 :as grpc.http2]
            [sf.firehose.v2.Fetch.client :as fire]
            [sf.ethereum.type.v2 :as eth]
            [dotenv :refer [env]]
            ))

(def firehose-endpoint "https://eth.firehose.pinax.network:443")
(def jwt (env "FIREHOSE_JWT"))

(def client @(grpc.http2/connect {:uri firehose-endpoint
                                  :ssl true
                                  :idle-timeout 60000
                                  :metadata {"authorization" jwt}}))

(def response @(fire/Block client {"blockNumber" {"num" 12345}}))

(eth/pb->Block (:value  (:block response)))
