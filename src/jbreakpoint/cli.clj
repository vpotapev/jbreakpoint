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

(defn buffer-append [context in-key]
  (swap! context conj {:buffer (conj (@context :buffer) in-key)}))

(defn history-append [context str]
  (swap! context conj {:history (conj (@context :history) str)}))

(defn move-cursor-left [context]
  nil)

(defn move-cursor-right [context]
  nil)

(defn process-in-key [context in-key]
  (case in-key
    Key$Kind/ArrowLeft (do (move-cursor-left context) true)
    Key$Kind/ArrowRight (do (move-cursor-left context) (true))
    false))

(defn input-loop [screen context]
  (while (not (@context :exit-flag))
    (do
      (def in-key (.readInput screen))
      (when (not= in-key nil)
        (process-in-key context in-key)
        (def ch (.getCharacter in-key))
        (buffer-append context ch)
        (.putCharacter (.getTerminal screen) ch)
        (if (= \q ch)
          (swap! context conj {:exit-flag true})))
      (if (.resizePending screen)
        (do
          (.updateScreenSize screen)
          (.refresh screen))))))

; clean input buffer
(defn clean-buf [buf]
  (swap! buf #(empty %)))

; return string which is transformed from a sequence of com.googlecode.lanterna.input.Key
(defn get-buf-as-string [buf]
  (string/join (map #(.getCharacter %) @buf)))

; get next cmd ended by EOL
(defn get-next-cmd [strbuf]
  (fnext (re-find #"(\S+)\n" strbuf)))

; return strbuf without first cmd (ended by EOL)
(defn pop-next-cmd [strbuf]
  (first (nnext (re-find #"(?s)(\S+?)\n(.*)" strbuf))))
