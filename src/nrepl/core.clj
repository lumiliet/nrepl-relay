(ns nrepl.core
  (:require
    [clojure.tools.nrepl :as repl]
    [clojure.java.io :as io]
    [clojure.pprint :as pprint]
    )
  (:import [java.net ServerSocket])
  (:gen-class))



(defn receive-message [socket]
  (.readLine (io/reader socket)))

(defn send-message [socket msg]
  (let [writer (io/writer socket)]
      (.write writer msg)
      (.flush writer)))

(defn send-repl-message [repl message] 
  (let [client (repl/client repl 1000)
        message-returned (repl/message client message)
        full-status (with-out-str (pprint/pprint (doall message-returned)))
        response-values (repl/response-values message-returned)
        ]
    (if (empty? response-values)
      full-status
      response-values
      )))


(defn receive-and-send [server repl] 
  (let [socket (.accept server)
        message (receive-message socket)]
    (println message)
    (send-message socket (str (send-repl-message repl (read-string message))))
    (.close socket)
    (receive-and-send server repl)))


(defn -main [] 
  (def ports {:server 19191 :repl 41913})
  (future
    (with-open
      [server (ServerSocket. (:server ports))
       repl (repl/connect :port (:repl ports))]
      (receive-and-send server repl)))

  (println "started server" ports))


(def verdi "heisann")


