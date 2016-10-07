(ns relapse.core
  (:require
    [clojure.tools.nrepl :as repl]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
    [clojure.pprint :as pprint])
  (:import [java.net ServerSocket])
  (:gen-class))

(defonce nrepl-clients (atom {}))

(defn receive-message [socket]
  (.readLine (io/reader socket)))


(defn send-message [socket msg]
  (let [writer (io/writer socket)]
    (.write writer msg)
    (.flush writer)))


(defn find-keyword-in-collection [coll keyw] 
  (reduce (fn [prev this] (or prev (keyw this)) )
          false
          coll))


(defn get-value-or-error [message] 
  (->> [:value :err :status]
       (map #(find-keyword-in-collection message %))
       (some identity)
       str))


(defn decode-message [message] 
  (json/read-json message))



(defn make-repl-client [port] 
  (let [client
        (-> (repl/connect :port port)
            (repl/client 1000))]
    (swap! nrepl-clients #(assoc % port client))
    client))




(defn get-repl-client [port] 
  (let [client (get @nrepl-clients port)]
    (if client
      client
      (make-repl-client port))))


(defn make-nrepl-message [message] 
  {:op :eval
   :code (:code message)
   :ns (:namespace message)})


(defn is-message-valid [message] 
  (not (some #(empty? (str %)) (vals message))))


(defn send-message-to-client [message] 
  (try 
    (if (is-message-valid message)
      (repl/message
        (get-repl-client (Integer. (:port message)))
        (make-nrepl-message message))
      [{:err "Empty code"}])
    (catch Exception e [{:err (str e)}])
  ))

(defn print-pipe [pipe] 
  (println pipe)
  pipe)

(defn send-repl-message [message] 
  (try (-> (json/read-json message)
           send-message-to-client
           get-value-or-error)
       (catch Exception e (str e))))


(defn server-loop [server] 
  (let [socket (.accept server)]
    (future
      (->> (receive-message socket)
           send-repl-message
           (send-message socket))
      (.close socket)))
  (server-loop server))

(def port 19191)

(defn -main [] 
  (with-open
    [server (ServerSocket. port)]
    (println "Started Relapse on port" port)
    (server-loop server)))

