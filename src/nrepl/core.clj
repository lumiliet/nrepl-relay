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

(defn find-keyword-in-collection [coll keyw] 
  (reduce
    (fn [prev this] (or prev (keyw this)) )
    false
    coll))


(defn get-value-or-error [message] 
  (some identity (map #(find-keyword-in-collection message %) [:value :err]))
  )


(defn lazy-to-string [lazy] 
  (with-out-str (pprint/pprint (doall lazy)))
  )

(defn send-repl-message [repl message] 
  (println "sending" message)
  (let [client (repl/client repl 1000)
        message-returned (repl/message client message)
        full-status (lazy-to-string message-returned)
        ]
    (println message-returned)
    (get-value-or-error message-returned)))


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


