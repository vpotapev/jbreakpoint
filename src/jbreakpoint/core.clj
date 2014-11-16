(ns jbreakpoint.core
  (:use [jbreakpoint.cli :as cli]
        [jbreakpoint.screen :as screen]))

(defn -main
  "Main function."
  [& args]
  (println "JBreakpoint started...")

  (def context (atom {
                       :buffer [] ; buffer for incoming keys (com.googlecode.lanterna.input.Key objects)
                       :exit-flag false ; exit flag
                       }))

  (screen/create-screen context cli/input-loop))
