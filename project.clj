(defproject responder "0.1.0-SNAPSHOT"
  :description "Testing Camunda Receive Messages"
  :url "http://github.com/lucasob"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [com.stuartsierra/component "1.1.0"]
                 [javax.xml.bind/jaxb-api "2.3.1"]
                 [org.camunda.bpm/camunda-external-task-client "7.18.0"]
                 [cheshire "5.11.0"]]
  :repl-options {:init-ns responder.core})
s