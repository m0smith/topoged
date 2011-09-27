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
		 [clojure-csv/clojure-csv "1.3.2"]
		 ]
  :dev-dependencies [[swank-clojure "1.3.3-20110809.143608-3"]])
