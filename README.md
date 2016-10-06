#relapse

Receives code and and sends it to an nrepl.

I could not work out the protocol to send messagees to the nrepl directly, so I made my own clojure program. It is probably not strictly necessary. But it works.

##Use

Use `lein run` to start the server. To build into a standalone java program you can run `lein uberjar`. Then start the server by running by running `java -jar target/relapse*standalone.jar` from the project root.
