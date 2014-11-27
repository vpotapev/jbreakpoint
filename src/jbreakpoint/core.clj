(ns jbreakpoint.core
  (:use [jbreakpoint.cli :as cli]
        [jbreakpoint.screen :as screen]))

(def context (atom {
                     :buffer [] ; buffer for incoming chars
                     :output-y-pos 0 ; starting output line in console (y-coord)
                     :buffer-pos 0 ; cursor pos in buffer. Can be changed by cursor keys
                     :history [] ; buffer for storing history (list of strings)
                     :exit-flag false ; exit flag
                     }))

(defn -main
  "Main function."
  [& args]
  (println "JBreakpoint started...")

  (screen/create-screen context cli/input-loop))
