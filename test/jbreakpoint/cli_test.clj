(ns jbreakpoint.cli-test
  (:require ;;[clojure.test :refer :all]
    [jbreakpoint.cli :as cli])
  (:use [expectations])
  (import (com.googlecode.lanterna.input Key)))

; input buffer (simulate incoming key press)
(def buf (atom []))

; test getting whole current buffer as string
(expect
  "abc123"
  (do
    (cli/clean-buf buf)
    (swap! buf into (map #(Key. %) [\a \b \c \1 \2 \3]))
    (cli/get-buf-as-string buf)))

; test getting next execution command (should be ended by CRLF)
(expect
  "list"
  (do
    (cli/clean-buf buf)
    (swap! buf into (map #(Key. %) (seq "list\ndel")))
    (cli/get-next-cmd (cli/get-buf-as-string buf))))

; test for removing next execution command from buffer
(expect
  "del\n"
  (do
    (cli/clean-buf buf)
    (swap! buf into (map #(Key. %) (seq "list\ndel\n")))
    (cli/pop-next-cmd (cli/get-buf-as-string buf))))

; test transformation of a keypress series to an appropriate command and command execution (test handlers for each command)
(def commands
  {
    #"list" #(identity :cmd-list); dummy handler. Should be filled after implementing the JDI interface
    #"list2" #(identity :cmd-list2); dummy handler
    })
(expect
  :cmd-list
  (do
    (cli/clean-buf buf)
    (swap! buf into (map #(Key. %) (seq "list\ndel")))
    (def nextcmd (cli/get-next-cmd (cli/get-buf-as-string buf)))
    (apply (second (first (filter #(re-find (key %) nextcmd) commands))) [])))

; TODO: test autocomplete feature - suggest for a typing command
