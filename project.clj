
(defproject topoged "1.0.0-SNAPSHOT"
  :description "Topoged"
  :target-dir "target/"

  :source-paths ["src/main/clojure"]
  :library-path "target/dependency"
  :test-paths ["src/test/clojure" "src/test/resources"]
  :resource-paths ["src/main/resources"]
  :main topoged.viewer.frame
  :jvm-opts [
             "-server" 
             ; "-Xms128M" "-Xmx256M"
             ;; Use these options in development to allow debugging
             ;; with jswat on localhost port 9900
             ;;"-Xdebug"
             ;;"-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=9900"
             ]
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [seesaw "1.4.3"]
                 [clj-glob "1.0.0"]
                 [org.clojure/data.codec "0.1.0"]
                 [org.clojure/core.logic "0.8.0-rc1"]
                 [quil "1.6.0"]
                 [slingshot "0.10.3"]
                 [clj-time "0.5.0"]
                 [com.taoensso/tower "2.0.0-beta5"] 
                 [clojurewerkz/archimedes "1.0.0-alpha5"]
                 [com.tinkerpop.blueprints/blueprints-core "2.4.0"]
                 [com.tinkerpop.blueprints/blueprints-graph-jung "2.4.0"]
                 [clojurewerkz/ogre "2.3.0.1"]
                 [net.sf.jung/jung-graph-impl "2.0.1"]
                 [net.sf.jung/jung-visualization "2.0.1"]
                 [org.clojure/tools.trace "0.7.5"]
		 ]
  :dev-dependencies [;;[swank-clojure "1.4.0"]
                     
                     [lein-eclipse "1.0.0"]])
