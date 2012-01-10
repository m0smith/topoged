(defproject topoged "1.0.0-SNAPSHOT"
  :description "Topoged"
  :target-dir "target/"

  :source-path "src/main/clojure"
  :library-path "target/dependency"
  :test-path "src/test/clojure"
  :resources-path "src/main/resources" 
  :dev-resources-path "src/test/resources"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [com.ashafa/clutch "0.3.0"]
                 [org.clojure/tools.logging "0.2.3"]
                 [org.slf4j/slf4j-api "1.6.2"]
                 [org.slf4j/slf4j-log4j12 "1.6.2"]
                 [org.slf4j/log4j-over-slf4j "1.6.2"]
                 [clj-glob "1.0.0"]
                 [org.clojure/data.codec "0.1.0"]
		 ]
  :dev-dependencies [[swank-clojure "1.3.4"]
                     [lein-eclipse "1.0.0"]])
