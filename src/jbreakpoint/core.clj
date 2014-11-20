(ns jbreakpoint.core
  (:use [jbreakpoint.cli :as cli]
        [jbreakpoint.screen :as screen]))

(def context (atom {
                     :buffer [] ; buffer for incoming keys (com.googlecode.lanterna.input.Key objects)
                     :exit-flag false ; exit flag
                     }))

(defn -main
  "Main function."
  [& args]
  (println "JBreakpoint started...")

  (screen/create-screen context cli/input-loop))
