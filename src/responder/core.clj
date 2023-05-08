(ns responder.core
  (:require [com.stuartsierra.component :as component]
            [responder.business-process-runtime.component :as camunda]
            [responder.processors.initial :as initial]
            [responder.processors.final :as final]))

(defn new-responder [{:keys [business-process-runtime-configuration]}]
  (component/system-map

    :business-process-runtime-configuration
    business-process-runtime-configuration

    :business-process-runtime
    (component/using
      (camunda/new-business-process-runtime)
      {:configuration :business-process-runtime-configuration})

    :initial-receive-processor
    (component/using
      (initial/new-processor)
      [:business-process-runtime])

    :finalise-processor
    (component/using
      (final/new-processor)
      [:business-process-runtime])))
