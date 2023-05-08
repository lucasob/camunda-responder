(ns responder.business-process-runtime.camunda
  (:require
    [cheshire.core :as json]
    [clojure.string :as str]
    [clojure.walk :as walk])
  (:import
    [clojure.lang Keyword Ratio Symbol]
    [java.util Collection Map UUID]
    [org.camunda.bpm.client ExternalTaskClient ExternalTaskClientBuilder]
    [org.camunda.bpm.client.task
     ExternalTask
     ExternalTaskHandler
     ExternalTaskService]
    [org.camunda.bpm.client.topic TopicSubscriptionBuilder]
    [org.camunda.bpm.client.variable ClientValues]
    [org.camunda.bpm.engine.variable Variables]))

(defn- extract-variables [^ExternalTask task]
  (walk/keywordize-keys (into {} (.getAllVariables task))))

(defn engine-url [{:keys [base-url]}]
  (str base-url "/engine-rest"))

(defn create-client
  "Create and initialise a Camunda external task client.
  https://docs.camunda.org/manual/7.17/user-guide/ext-client/"
  [base-url & {:keys [lock-duration
                      worker-id
                      max-tasks
                      async-resp-timeout
                      use-priority?
                      interceptors
                      auto-fetch
                      backoff-strategy
                      serialization-format
                      date-format]}]
  (let [^ExternalTaskClientBuilder builder (ExternalTaskClient/create)]
    (.baseUrl builder base-url)
    (when lock-duration
      (.lockDuration builder lock-duration))
    (when worker-id
      (.workerId builder worker-id))
    (when max-tasks
      (.maxTasks builder max-tasks))
    (when async-resp-timeout
      (.asyncResponseTimeout builder async-resp-timeout))
    (when serialization-format
      (.defaultSerializationFormat builder serialization-format))
    (when date-format
      (.dateFormat builder date-format))
    (when-not (nil? use-priority?)
      (.usePriority builder use-priority?))
    (when (coll? interceptors)
      (doseq [interceptor interceptors]
        (.addInterceptor builder interceptor)))
    (when (false? auto-fetch)
      (.disableAutoFetching builder))
    (when-not (nil? backoff-strategy)
      (if (false? backoff-strategy)
        (.disableBackoffStrategy builder)
        (.backoffStrategy builder backoff-strategy)))
    (.build builder)))

(defn- build-handler [handler-fn]
  (reify ExternalTaskHandler
    (^void execute [_ ^ExternalTask task ^ExternalTaskService service]
      (let [variables (extract-variables task)]
        (try
          (handler-fn task service variables)
          (catch Exception e
            (prn {:variables variables
                  :topic     (.getTopicName task)
                  :task-id   (.getId task)
                  :message   "Failed to execute task handler"})))))))

(defn subscribe
  "Attach a handler to a topic on a Camunda external task client."
  [^ExternalTaskClient client
   topic
   handler &
   {:keys [lock-duration
           variables
           local-variables?
           business-key
           process-definition-id
           process-definition-key
           process-definition-version-tag
           process-variables-equals
           tenant-ids
           include-extension-properties?]}]
  (let [^TopicSubscriptionBuilder subb (.subscribe client topic)]
    (.handler subb (build-handler handler))
    (when lock-duration
      (.lockDuration subb lock-duration))
    (when business-key
      (.businessKey subb business-key))
    (when process-definition-id
      (if (coll? process-definition-id)
        (.processDefinitionIdIn subb (into-array String process-definition-id))
        (.processDefinitionId subb process-definition-id)))
    (when process-definition-key
      (if (coll? process-definition-key)
        (.processDefinitionKeyIn subb (into-array String process-definition-key))
        (.processDefinitionKey subb process-definition-key)))
    (when process-definition-version-tag
      (.processDefinitionVersionTag subb process-definition-version-tag))
    (when-not (nil? local-variables?)
      (.localVariables subb local-variables?))
    (when (seq variables)
      (.variables subb (into-array String variables)))
    (when (coll? tenant-ids)
      (if (seq tenant-ids)
        (.tenantIdIn subb (into-array String tenant-ids))
        (.withoutTenantId subb)))
    (when-not (nil? include-extension-properties?)
      (.includeExtensionProperties subb include-extension-properties?))
    (when (seq process-variables-equals)
      (.processVariablesEqualsIn subb process-variables-equals))
    (.open subb)))

(defn encode-camunda-data [data]
  (json/generate-string data))

(defprotocol VariableSerialise
  (serialise [this] "Serialise object as Camunda variable."))

(extend-protocol VariableSerialise
  nil (serialise [_] (Variables/untypedNullValue))
  String (serialise [this] (Variables/stringValue this))
  Character (serialise [this] (Variables/stringValue (str this)))
  Keyword (serialise [this] (Variables/stringValue (str/join (rest (str this)))))
  Symbol (serialise [this] (Variables/stringValue (str this)))
  Boolean (serialise [this] (Variables/booleanValue this))
  Short (serialise [this] (Variables/shortValue this))
  Integer (serialise [this] (Variables/integerValue this))
  Long (serialise [this] (Variables/longValue this))
  Double (serialise [this] (Variables/doubleValue this))
  Float (serialise [this] (Variables/doubleValue (double this)))
  Ratio (serialise [this] (Variables/doubleValue (double this)))
  Collection (serialise [this] (ClientValues/jsonValue (encode-camunda-data this)))
  Map (serialise [this] (ClientValues/jsonValue (encode-camunda-data this)))
  UUID (serialise [this] (Variables/stringValue (str this))))

(extend (class (byte-array 0))
  VariableSerialise
  {:->variable (fn [this] (Variables/byteArrayValue this))})
