(ns
  ^{:author rhub}
  jbreakpoint.cli
  (:require [clojure.string :as string]
            [taoensso.timbre :as timbre])
  (:import (java.nio.charset Charset)
           (com.googlecode.lanterna TerminalFacade)
           (com.googlecode.lanterna.gui Action GUIScreen Window Component)
           (com.googlecode.lanterna.gui.component Button Label Panel Table)
           (com.googlecode.lanterna.gui.layout LinearLayout VerticalLayout)
           (com.googlecode.lanterna.terminal Terminal TerminalSize)
           (com.googlecode.lanterna.terminal.text UnixTerminal)
           (com.googlecode.lanterna.input Key)
           (com.googlecode.lanterna.screen ScreenCharacterStyle)))
(timbre/refer-timbre)

(defn print-buffer-line [screen context]
  (let [buf (@context :buffer)
        term-width (.getColumns (.getTerminalSize screen))
        term-height (.getRows (.getTerminalSize screen))
        buf-len (count buf)
        cursor-pos (@context :buffer-pos)
        output-y-pos (@context :output-y-pos)]
    ; output buffer piece by piece
    (loop [buf-for-print buf
           y-pos output-y-pos]
      (def output-str (string/join (take term-width buf-for-print)))
      (trace "buf-for-print: " buf-for-print "  y-pos: " y-pos "  output-str: " output-str)
      (.putString screen 0 y-pos output-str
        com.googlecode.lanterna.terminal.Terminal$Color/WHITE
        com.googlecode.lanterna.terminal.Terminal$Color/BLACK #{})
      (if (< term-width (count buf-for-print))
        (recur (into [] (take-last (- (count buf-for-print) term-width) buf-for-print)) (inc y-pos))))
    ; set cursor position
    (.setCursorPosition screen
      (rem cursor-pos term-width)
      (+ (quot cursor-pos term-width) output-y-pos))))

(defn history-append [context str]
  (swap! context conj {:history (conj (@context :history) str)}))

(defn move-cursor-left [context]
  (trace "move-cursor-left called")
  (let [cursor-pos (@context :buffer-pos)]
    (if (> cursor-pos 0)
      (swap! context conj {:buffer-pos (dec cursor-pos)}))))

(defn move-cursor-right [context]
  (trace "move-cursor-right called")
  (let [buf (@context :buffer)
        buf-len (count buf)
        cursor-pos (@context :buffer-pos)]
    (if (< cursor-pos buf-len)
      (swap! context conj {:buffer-pos (inc cursor-pos)}))))

(defn move-cursor-on-bof [context]
  (trace "move-cursor-on-bof called")
  (swap! context conj {:buffer-pos 0}))

(defn move-cursor-on-eof [context]
  (trace "move-cursor-on-eof called")
  (let [buf (@context :buffer)
        buf-len (count buf)]
    (swap! context conj {:buffer-pos buf-len})))

(defn buffer-insert [context ch]
  (trace "buffer-insert called")
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)
        before-part (subvec buf 0 cursor-pos)
        after-part (subvec buf cursor-pos)
        new-buf (concat before-part [ch] after-part)]
    (swap! context conj {:buffer (into [] new-buf)})
    (move-cursor-right context)))

(defn delete-before-cursor [context]
  (trace "delete-before-cursor called")
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)]
    (if (and (> (count buf) 0) (> cursor-pos 0))
      (let [before-part (subvec buf 0 (- cursor-pos 1))
            after-part (subvec buf cursor-pos)
            new-buf (concat before-part after-part)]
        (swap! context conj {:buffer (into [] new-buf)})
        (move-cursor-left context)))))

(defn delete-under-cursor [context]
  (trace "delete-under-cursor called")
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)]
    (if (and (> (count buf) 0) (< cursor-pos (count buf)))
      (let [before-part (subvec buf 0 cursor-pos)
            after-part (subvec buf (+ cursor-pos 1))
            new-buf (concat before-part after-part)]
        (swap! context conj {:buffer (into [] new-buf)})))))

(defn process-in-key [context in-key]
  (trace "process-in-key called")
  (condp = (.getKind in-key) ; TODO: can be replaced by core.match (one switch for all cases) as part of input loop
    com.googlecode.lanterna.input.Key$Kind/NormalKey (do
                                                       (def ch (.getCharacter in-key))
                                                       (buffer-insert context ch)
                                                       (if (= ch \q)
                                                         (swap! context conj {:exit-flag true})) false)
    com.googlecode.lanterna.input.Key$Kind/ArrowLeft (do (move-cursor-left context) true)
    com.googlecode.lanterna.input.Key$Kind/ArrowRight (do (move-cursor-right context) true)
    com.googlecode.lanterna.input.Key$Kind/Backspace (do (delete-before-cursor context) true)
    com.googlecode.lanterna.input.Key$Kind/Delete (do (delete-under-cursor context) true)
    com.googlecode.lanterna.input.Key$Kind/Home (do (move-cursor-on-bof context) true)
    com.googlecode.lanterna.input.Key$Kind/End (do (move-cursor-on-eof context) true)
    false))

(defn input-loop [screen context]
  (trace "input-loop called")
  (while (not (@context :exit-flag))
    (def in-key (.readInput screen))
    (when (not= in-key nil)
      (trace "in-key: " in-key)
      (def key-kind (.getKind in-key))
      (process-in-key context in-key)
      (.clear screen)
      (print-buffer-line screen context)
      (.refresh screen)
      (trace "context: " @context))
    (if (.resizePending screen)
      (do
        (.updateScreenSize screen)
        (.refresh screen)))
    ))

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
