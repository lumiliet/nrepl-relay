(ns nrepl.core
  (:require
    [clojure.tools.nrepl :as repl]
    [clojure.java.io :as io]
    [clojure.data.json :as json]
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
  (reduce (fn [prev this] (or prev (keyw this)) )
          false
          coll))


(defn get-value-or-error [message] 
  (some identity (map
                   #(find-keyword-in-collection message %)
                   [:value :err])))


(defn lazy-to-string [lazy] 
  (with-out-str (pprint/pprint (doall lazy))))


(defn decode-message [message] 
  (->> message
       json/read-json))


(defn message-to-nrepl [message] 
  (let [decoded (decode-message message)]
    {:op :eval
     :code (:code decoded)
     :ns (:namespace decoded)}))


(defn send-repl-message [repl message] 
  (try (let [client (repl/client repl 1000)
             message-returned (repl/message
                                client
                                (message-to-nrepl message))]
         (get-value-or-error message-returned))
       (catch Exception e "Everything is fucked")))


(defn receive-and-send [server repl] 
  (let [socket (.accept server)]
    (future (let [message (receive-message socket)]
              (send-message socket (send-repl-message repl message))
            (.close socket)))
    (receive-and-send server repl)))


(defn -main [repl-port] 
  (def ports {:server 19191 :repl (Integer. repl-port)})
  (future
    (with-open
      [server (ServerSocket. (:server ports))
       repl (repl/connect :port (:repl ports))]
      (println "started server" ports)
      (receive-and-send server repl))))

