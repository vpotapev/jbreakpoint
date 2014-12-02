(defproject jbreakpoint "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [expectations "2.0.9"]
                 [lein-git-deps "0.0.1-SNAPSHOT"]
                 [com.googlecode.lanterna/lanterna "2.1.8"]
                 [com.taoensso/timbre "3.3.1"]]
  :plugins [[lein-expectations "0.0.7"]]
  ;;:git-dependencies [["https://github.com/clojure/clojurescript.git"]]
  :main ^:skip-aot jbreakpoint.core
  ;;:java-source-paths ["com.googlecode.lanterna"]
  :target-path "target/%s"
  ;;:aot [com.googlecode.lanterna]
  :profiles {:uberjar {:aot :all}})
