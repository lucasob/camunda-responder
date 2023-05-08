(ns responder.business-process-runtime.component
  (:require
    [com.stuartsierra.component :as component]
    [responder.business-process-runtime.camunda :as camunda])
  (:import
    [org.camunda.bpm.client ExternalTaskClient]))

(defrecord BusinessProcessRuntime [configuration]
  component/Lifecycle
  (start [component]
    (assoc component
      :client (camunda/create-client (camunda/engine-url configuration))
      :configuration configuration
      :workflows {}))
  (stop [{:keys [^ExternalTaskClient client]}]
    (when (-> client
              (and)
              (.isActive))
      (.stop client))))

(defn new-business-process-runtime []
  (map->BusinessProcessRuntime {}))
