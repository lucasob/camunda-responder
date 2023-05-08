# responder

A clojure application demonstrating how to model asynchronous tasks with Camunda, by using `Receive Message` Tasks.

This particular repository deals with the workflow defined
in [process_receive_1.bpmn](./resources/process_receive_1.bpmn)

It contains examples of

* defining handlers
* accessing variables
* serializing new variables

There are no tests. Don't @ me. Sometimes it makes little sense to put the horse before the cart.

## Getting Started

### Camunda Modeller

Download the executable at `https://camunda.com/download/modeler/`

### Camunda Platform

Download the docker image via dockerhub

```
╰─>$ docker pull camunda/camunda-bpm-platform
```

Run the camunda platform

```
╰─>$ docker run --rm -d --name camunda -p 8080:8080 camunda/camunda-bpm-platform
```

Navigate to the `camunda cockpit` at `http://localhost:8080/camunda/app/cockpit/default/#/dashboard`

## Invocation & Using the System

### Build a Workflow

Spin up the modeller and get drawing. Be sure to save the file.

### Create new deployment from existing BPMN file

```
curl --location --request POST 'localhost:8080/engine-rest/deployment/' \
--form 'upload=@"/path/to/file.bpnm"'
```

### Get process definition

```
curl --location --request GET 'localhost:8080/engine-rest/process-definition/ID_FROM_ABOVE'
```

### Start an instance of a process

```
curl --location --request POST 'localhost:8080/engine-rest/process-definition/ID_FROM_ABOVE/start' \
--header 'Content-Type: application/json' \
--data-raw '{}'
```

### Send a message to a running process instance

```
curl --location --request POST 'localhost:8080/engine-rest/message' \
--header 'Content-Type: application/json' \
--data-raw '{
    "messageName": "MESSAGE_NAME",
    "processInstanceId": "PROCESS_INSTANCE_ID"
}'
```

