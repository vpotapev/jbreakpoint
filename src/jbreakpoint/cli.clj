(ns
  ^{:author rhub}
  jbreakpoint.cli
  (:require [clojure.string :as string])
  (:import (java.nio.charset Charset)
           (com.googlecode.lanterna TerminalFacade)
           (com.googlecode.lanterna.gui Action GUIScreen Window Component)
           (com.googlecode.lanterna.gui.component Button Label Panel Table)
           (com.googlecode.lanterna.gui.layout LinearLayout VerticalLayout)
           (com.googlecode.lanterna.terminal TerminalSize)
           (com.googlecode.lanterna.terminal.text UnixTerminal)
           (com.googlecode.lanterna.input Key)))

(def event-queue (atom []))

(defn buffer-append [in-key]
  )

(defn input-loop [screen context]
  (def exit-flag (atom false))
  (while @exit-flag
    (do
      (def in-key (.readInput screen))
      (if (not= in-key nil)
        (swap! context buffer-append in-key))
      (if (.resizePending screen)
        (.refresh screen)))))

(defn clean-buf [buf]
  (swap! buf #(empty %)))

(defn get-buf-as-string [buf]
  (string/join (map #(.getCharacter %) @buf)))

(defn get-next-cmd [strbuf]
  (fnext (re-find #"(\S.+)\n" strbuf)))
