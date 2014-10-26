(ns jbreakpoint.core
  (:use [jbreakpoint.cli :as cli]
        [jbreakpoint.screen :as screen]))

(defn -main
  "Main function."
  [& args]
  (println "JBreakpoint started...")

  (def context (atom {}))

  (screen/create-screen context cli/input-loop))
