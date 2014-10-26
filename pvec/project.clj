(defproject com.hypirion/pvec "0.1.0-SNAPSHOT"
  :description "Persistent vector implementation in Java"
  :url "https://github.com/hyPiRion/pvec-perf"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths []
  :java-source-paths ["src"]
  :javac-options ["-target" "1.6" "-source" "1.6"]
  :aliases {"test" ["run" "-m" "com.hypirion.pvec.tests.Test"]}
  :jvm-opts []
  :profiles {:dev {:java-source-paths ["test"]
                   :dependencies [[org.clojure/clojure "1.6.0"]]}})
