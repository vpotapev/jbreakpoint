(ns jbreakpoint.cli-test
  (:use ;;[clojure.test :refer :all]
        [jbreakpoint.cli :as c]
        [expectations]))

; TODO: need to implement

; test input buffer (simulate incoming key press)

; test transformation of a keypress series to an appropriate command

; test of a command execution (test handlers for each command)

(defn commands
  {
    :attach #() ; empty handler now. whould be filled after implementing the JDI interface
    })

(expect ((complement nil?) 1))
