(defproject topoged "1.0.0-SNAPSHOT"
  :description "FIXME: write"
  :target-dir "target/"

  :source-path "src/main/clojure"
  :library-path "target/dependency"
  :test-path "src/test/clojure"
  :resources-path "src/main/resources" 
  :dev-resources-path "src/test/resources"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/clojure-contrib "1.2.0"]
		 [com.miglayout/miglayout "3.7.3" :classifier "swing"]
		 [org.hibernate/hibernate-annotations "3.4.0.GA"]
		 [org.slf4j/slf4j-api "1.6.2"]
		 [org.slf4j/slf4j-log4j12 "1.6.2"]
		 [org.slf4j/log4j-over-slf4j "1.6.2"]
		 [org.apache.derby/derby "10.1.1.0"]
		 [clojure-csv/clojure-csv "1.3.2"]
		 [clj-glob "1.0.0"]
		 [log4j "1.2.16"]
		 ]
  :dev-dependencies [[swank-clojure "1.3.3-20110809.143608-3"]
		     [lein-eclipse "1.0.0"]])
