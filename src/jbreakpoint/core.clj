(ns jbreakpoint.core
  (:gen-class)
  (:use [jbreakpoint.cli
         jbreakpoint.screen]))

(defn -main
  "Main function."
  [& args]
  (println "JBreakpoint started...")

  (def context (atom {}))

  (jbreakpoint.screen/create-screen context jbreakpoint.cli/input-loop))
