(ns jbreakpoint.core
  (:use [jbreakpoint.cli :as cli]
        [jbreakpoint.screen :as screen]
        [jbreakpoint.core])
  (:require [taoensso.timbre :as timbre])
  (:gen-class))
(timbre/refer-timbre)

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

  (timbre/set-config! [:appenders :standard-out :enabled?] false)
  (timbre/set-config! [:appenders :spit :enabled?] true)
  (timbre/set-config! [:shared-appender-config :spit-filename] "my-file.log")

  (info "JBreakpoint started")
  (screen/create-screen context cli/input-loop)
  (info "JBreakpoint stopped"))
