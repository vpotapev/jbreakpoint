(ns jbreakpoint.cli-test
  (:require ;;[clojure.test :refer :all]
    [jbreakpoint.cli :as cli])
  (:use [expectations])
  (import (com.googlecode.lanterna.input Key)))

; input buffer (simulate incoming key press)
(def buf (atom []))

; test getting whole current buffer as string
(expect (do
          (cli/clean-buf buf)
          (swap! buf into (map #(Key. %) [\a \b \c \1 \2 \3]))
          (cli/get-buf-as-string buf))
  "abc123")

; test getting next execution command (should be ended by CRLF)
(expect (do
          (cli/clean-buf buf)
          (swap! buf into (map #(Key. %) (seq "list\ndel")))
          (cli/get-next-cmd (cli/get-buf-as-string buf)))
  "list")

; test transformation of a keypress series to an appropriate command
(def commands
  {
    #"list" #(identity :cmd-list); dummy handler now. Should be filled after implementing the JDI interface
    })
(expect (do
          (cli/clean-buf buf)
          (swap! buf into (map #(Key. %) (seq "list\ndel")))
          (cli/get-next-cmd (cli/get-buf-as-string buf)))
  :cmd-list)


; test of a command execution (test handlers for each command)


; test autocomplete feature - suggest for a typing command


(expect (complement nil?) 1)
