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
  (let [cursor-pos (@context :buffer-pos)]
    (if (> cursor-pos 0)
      (swap! context conj {:buffer-pos (dec cursor-pos)}))))

(defn move-cursor-right [context]
  (let [buf (@context :buffer)
        buf-len (count buf)
        cursor-pos (@context :buffer-pos)]
    (if (< cursor-pos buf-len)
      (swap! context conj {:buffer-pos (inc cursor-pos)}))))

(defn move-cursor-on-bof [context]
  (swap! context conj {:buffer-pos 0}))

(defn move-cursor-on-eof [context]
  (let [buf (@context :buffer)
        buf-len (count buf)]
    (swap! context conj {:buffer-pos buf-len})))

(defn move-cursor-one-word-left [context]
  (let [buf (into [] (@context :buffer))
        buf-len (count buf)
        cursor-pos (@context :buffer-pos)
        before-part (subvec buf 0 cursor-pos)
        strbuf (string/trimr (string/join before-part))
        newindex (.lastIndexOf strbuf " ")]
    (if (not= newindex -1)
      (swap! context conj {:buffer-pos (inc newindex)})
      (swap! context conj {:buffer-pos 0}))))

(defn move-cursor-one-word-right [context]
  (let [buf (into [] (@context :buffer))
        buf-len (count buf)
        cursor-pos (@context :buffer-pos)
        strbuf (string/join buf)
        newindex (.lastIndexOf strbuf " ")
        started-from-space (if (not= buf-len cursor-pos)
                             (= (buf cursor-pos) \space)
                             false)]
    (while (and
             (< (@context :buffer-pos) buf-len)
             (= (buf (@context :buffer-pos)) \space))
      (do
        (move-cursor-right context)))
    (while (and
             (not started-from-space)
             (< (@context :buffer-pos) buf-len)
             (not= (buf (@context :buffer-pos)) \space))
      (do
        (move-cursor-right context)))
    (while (and
             (not started-from-space)
             (< (@context :buffer-pos) buf-len)
             (= (buf (@context :buffer-pos)) \space))
      (do
        (move-cursor-right context)))))

(defn buffer-insert [context ch]
  (trace "buffer-insert called")
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)
        before-part (subvec buf 0 cursor-pos)
        after-part (subvec buf cursor-pos)
        new-buf (concat before-part [ch] after-part)]
    (swap! context conj {:buffer (into [] new-buf)})
    (trace "buffer: " (@context :buffer))
    (move-cursor-right context)))

(defn in-key-buffer-append [context in-key]
  (let [buf (@context :in-key-buffer)
        new-buf (concat buf [in-key])]
    (swap! context conj {:in-key-buffer new-buf})))

(defn in-key-buffer-clear [context]
  (swap! context conj {:in-key-buffer []}))

(defn delete-before-cursor [context]
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)]
    (if (and (> (count buf) 0) (> cursor-pos 0))
      (let [before-part (subvec buf 0 (- cursor-pos 1))
            after-part (subvec buf cursor-pos)
            new-buf (concat before-part after-part)]
        (swap! context conj {:buffer (into [] new-buf)})
        (move-cursor-left context)))))

(defn delete-under-cursor [context]
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)]
    (if (and (> (count buf) 0) (< cursor-pos (count buf)))
      (let [before-part (subvec buf 0 cursor-pos)
            after-part (subvec buf (+ cursor-pos 1))
            new-buf (concat before-part after-part)]
        (swap! context conj {:buffer (into [] new-buf)})))))

(defn process-in-key [context in-key]
  (in-key-buffer-append context in-key)
  (let [in-key-buf (@context :in-key-buffer)
        in-key-buf-len (count in-key-buf)
        f (first in-key-buf)
        s (second in-key-buf)]
    (case in-key-buf-len
      1 (condp = (.getKind f)
          com.googlecode.lanterna.input.Key$Kind/NormalKey (if (.equals f (Key. \O false true))
                                                             (identity false)
                                                             (do
                                                               (def ch (.getCharacter f))
                                                               (buffer-insert context ch)
                                                               (if (= ch \q)
                                                                 (swap! context conj {:exit-flag true}))
                                                               (in-key-buffer-clear context) false))
          com.googlecode.lanterna.input.Key$Kind/ArrowLeft (do
                                                             (move-cursor-left context)
                                                             (in-key-buffer-clear context) true)
          com.googlecode.lanterna.input.Key$Kind/ArrowRight (do
                                                              (move-cursor-right context)
                                                              (in-key-buffer-clear context) true)
          com.googlecode.lanterna.input.Key$Kind/Backspace (do
                                                             (delete-before-cursor context)
                                                             (in-key-buffer-clear context) true)
          com.googlecode.lanterna.input.Key$Kind/Delete (do
                                                          (delete-under-cursor context)
                                                          (in-key-buffer-clear context) true)
          com.googlecode.lanterna.input.Key$Kind/Home (do
                                                        (move-cursor-on-bof context)
                                                        (in-key-buffer-clear context) true)
          com.googlecode.lanterna.input.Key$Kind/End (do
                                                       (move-cursor-on-eof context)
                                                       (in-key-buffer-clear context) true)
          false)
      2 (if (.equals f (Key. \O false true)) ; process case of six keys section on a keyboard
          (condp = s
            (Key. \H false false) (do
                                    (move-cursor-on-bof context)
                                    (in-key-buffer-clear context) true)
            (Key. \F false false) (do
                                    (move-cursor-on-eof context)
                                    (in-key-buffer-clear context) true)
            false))
      6 (if (.equals f (Key. com.googlecode.lanterna.input.Key$Kind/Escape)) ; processing Escape-sequence
          (let [key-seq (string/join (map #(.getCharacter %) (subvec (into [] in-key-buf) 1)))]
            (condp = key-seq
              ; Ctrl+Left
              "[1;5D" (do
                        (move-cursor-one-word-left context)
                        (in-key-buffer-clear context) true)
              ; Ctrl+Right
              "[1;5C" (do
                        (move-cursor-one-word-right context)
                        (in-key-buffer-clear context) true)
              false)))
      false)))

(defn input-loop [screen context]
  (while (not (@context :exit-flag))
    (def in-key (.readInput screen))
    (when (not= in-key nil)
      (process-in-key context in-key)
      (.clear screen)
      (print-buffer-line screen context)
      (.refresh screen))
    (if (.resizePending screen)
      (do
        (.updateScreenSize screen)
        (.refresh screen)))))

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
