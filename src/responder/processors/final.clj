(ns responder.processors.final
  (:require
    [com.stuartsierra.component :as component]
    [responder.business-process-runtime.camunda :as camunda]
    [clojure.pprint :refer [pprint]]
    [cheshire.core :as json])
  (:import [org.camunda.bpm.client.task ExternalTask ExternalTaskService]))

(defn- handler
  [^ExternalTask task
   ^ExternalTaskService service
   variables]
  (pprint variables)
  (json/parse-string (:initial variables) true)
  (.complete service task))

(defrecord Processor [business-process-runtime]
  component/Lifecycle
  (start [component]
    (let [topic "FINALISE"
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