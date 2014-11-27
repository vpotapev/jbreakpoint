(ns
  ^{:author rhub}
  jbreakpoint.cli
  (:require [clojure.string :as string])
  (:import (java.nio.charset Charset)
           (com.googlecode.lanterna TerminalFacade)
           (com.googlecode.lanterna.gui Action GUIScreen Window Component)
           (com.googlecode.lanterna.gui.component Button Label Panel Table)
           (com.googlecode.lanterna.gui.layout LinearLayout VerticalLayout)
           (com.googlecode.lanterna.terminal Terminal TerminalSize)
           (com.googlecode.lanterna.terminal.text UnixTerminal)
           (com.googlecode.lanterna.input Key)
           (com.googlecode.lanterna.screen ScreenCharacterStyle)))

(defn print-buffer-line [screen context]
  (let [buf (@context :buffer)
        term-width (.getColumns (.getTerminalSize screen))
        term-height (.getRows (.getTerminalSize screen))
        buf-len (count buf)
        cursor-pos (@context :buffer-pos)
        output-y-pos (@context :output-y-pos)]
  (.putString screen 0 output-y-pos (string/join buf)
    com.googlecode.lanterna.terminal.Terminal$Color/WHITE
    com.googlecode.lanterna.terminal.Terminal$Color/BLACK #{})
    ;ScreenCharacterStyle/Bold)
  (.setCursorPosition screen
    (rem cursor-pos term-width)
    (+ (quot cursor-pos term-width) output-y-pos))))

(defn buffer-insert [context ch]
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)
        before-part (subvec buf 0 cursor-pos)
        after-part (subvec buf cursor-pos)
        new-buf (concat before-part [ch] after-part)]
    (swap! context conj {:buffer (into [] new-buf)})))

(defn history-append [context str]
  (swap! context conj {:history (conj (@context :history) str)}))

(defn move-cursor-left [context]
  (swap! context conj {:buffer-pos (dec (@context :buffer-pos))}))

(defn move-cursor-right [context]
  (swap! context conj {:buffer-pos (inc (@context :buffer-pos))}))

(defn process-in-key [context in-key]
  (case in-key
    Key$Kind/ArrowLeft (do (move-cursor-left context) true)
    Key$Kind/ArrowRight (do (move-cursor-left context) true)
    false))

(defn input-loop [screen context]
  (while (not (@context :exit-flag))
    (do
      (def in-key (.readInput screen))
      (when (not= in-key nil)
        (process-in-key context in-key)
        (def ch (.getCharacter in-key))
        (buffer-insert context ch)
        (.clear screen)
        (print-buffer-line screen context)
        (.refresh screen)
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
