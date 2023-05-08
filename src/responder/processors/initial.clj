(ns responder.processors.initial
  (:require
    [com.stuartsierra.component :as component]
    [responder.business-process-runtime.camunda :as camunda])
  (:import [org.camunda.bpm.client.task ExternalTask ExternalTaskService]
           (java.time LocalTime)))

(defn- handler
  [^ExternalTask task
   ^ExternalTaskService service
   _]
  (prn task)
  (->> {:initial-request-time (str (LocalTime/now))}
       (camunda/serialise)
       (hash-map "initial")
       (.complete service task)))

(defrecord Processor [business-process-runtime]
  component/Lifecycle
  (start [component]
    (let [topic "INITIAL_RECEIVED"
          process-definition-key (get-in business-process-runtime [:configuration :process-definition-key])]
      (camunda/subscribe
        (:client business-process-runtime)
        topic
        handler
        :process-definition-key process-definition-key)
      (assoc component
        :topic topic
        :process-definition-key process-definition-key
        :runtime business-process-runtime)))
  (stop [component] component))

(defn new-processor []
  (map->Processor {}))