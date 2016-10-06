(defproject relapse "1.0.0"
  :description "Sends code to nrepl"
  :main relapse.core
  :url "https://github.com/lumiliet/relapse"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/data.json "0.2.6"]
                 [org.clojure/tools.nrepl "0.2.12"] ])
