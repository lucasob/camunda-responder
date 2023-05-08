(ns responder.main
  (:require [responder.core :as responder]
            [com.stuartsierra.component :as component]))

(defn -main [& _]
  (let [config {:business-process-runtime-configuration {:base-url "http://localhost:8080"
                                                         :process-definition-key "PROCESS_RECEIVE_1"}}
        responder (-> config
                      (responder/new-responder)
                      (component/start-system))]
    (.addShutdownHook (Runtime/getRuntime)
                      (new Thread #(component/stop-system responder)))))
