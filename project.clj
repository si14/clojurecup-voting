(defproject clojurecup-voting "0.1.0-SNAPSHOT"
  :description "A little thingie to log ClojureCup-2014 user voting"
  :url "http://github.com/si14/clojurecup-voting"
  :license {:name "CC0"
            :url "https://creativecommons.org/publicdomain/zero/1.0/legalcode"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [clj-http "1.0.0"]
                 [clj-time "0.8.0"]
                 [cheshire "5.3.1"]]

  :main ^:skip-aot clojurecup-voting.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
