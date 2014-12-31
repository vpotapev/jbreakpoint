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

(defn delete-char-before-cursor [context]
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)]
    (if (and (> (count buf) 0) (> cursor-pos 0))
      (let [before-part (subvec buf 0 (- cursor-pos 1))
            after-part (subvec buf cursor-pos)
            new-buf (concat before-part after-part)]
        (swap! context conj {:buffer (into [] new-buf)})
        (move-cursor-left context)))))

(defn delete-char-under-cursor [context]
  (let [buf (@context :buffer)
        cursor-pos (@context :buffer-pos)]
    (if (and (> (count buf) 0) (< cursor-pos (count buf)))
      (let [before-part (subvec buf 0 cursor-pos)
            after-part (subvec buf (+ cursor-pos 1))
            new-buf (concat before-part after-part)]
        (swap! context conj {:buffer (into [] new-buf)})))))

(defn delete-one-word-left [context]
  (trace "delete-one-word-left")
  (while (and (> (@context :buffer-pos) 0) (= ((@context :buffer) (- (@context :buffer-pos) 1)) \space))
    (do
      (delete-char-before-cursor context)))
  (while (and (> (@context :buffer-pos) 0) (not= ((@context :buffer) (- (@context :buffer-pos) 1)) \space))
    (do
      (delete-char-before-cursor context)))); TODO: should be checked on PC

(defn delete-one-word-right [context]
  (trace "delete-one-word-right")
  (while (and (< (@context :buffer-pos) (count (@context :buffer))) (= ((@context :buffer) (@context :buffer-pos)) \space))
    (do
      (delete-char-under-cursor context)))
  (while (and (< (@context :buffer-pos) (count (@context :buffer))) (not= ((@context :buffer) (@context :buffer-pos)) \space))
    (do
      (delete-char-under-cursor context)))); TODO: should be checked on PC

(defn process-in-key [context in-key]
  (in-key-buffer-append context in-key)
  (let [in-key-buf (@context :in-key-buffer)
        in-key-buf-len (count in-key-buf)
        f (first in-key-buf)
        s (second in-key-buf)]
    (trace "context: " @context "    f: " f " " [(.getKind f) (.isAltPressed f) (.isCtrlPressed f)])
    (case in-key-buf-len
      1 (condp = [(.getKind f) (.isAltPressed f) (.isCtrlPressed f)]
          [com.googlecode.lanterna.input.Key$Kind/NormalKey false false] (if (.equals f (Key. \O false true))
                                                                           (identity false)
                                                                           (do
                                                                             (def ch (.getCharacter f))
                                                                             (buffer-insert context ch)
                                                                             (if (= ch \q)
                                                                               (swap! context conj {:exit-flag true}))
                                                                             (in-key-buffer-clear context) false))
          [com.googlecode.lanterna.input.Key$Kind/ArrowLeft false false] (do
                                                                           (move-cursor-left context)
                                                                           (in-key-buffer-clear context) true)
          [com.googlecode.lanterna.input.Key$Kind/ArrowRight false false] (do
                                                                            (move-cursor-right context)
                                                                            (in-key-buffer-clear context) true)
          [com.googlecode.lanterna.input.Key$Kind/Backspace false false] (do
                                                                           (delete-char-before-cursor context)
                                                                           (in-key-buffer-clear context) true)
          [com.googlecode.lanterna.input.Key$Kind/Delete false false] (do
                                                                        (delete-char-under-cursor context)
                                                                        (in-key-buffer-clear context) true)
          [com.googlecode.lanterna.input.Key$Kind/Backspace false true] (do ; // TODO: should work only on PC
                                                                          (delete-one-word-left context)
                                                                          (in-key-buffer-clear context) true)
          [com.googlecode.lanterna.input.Key$Kind/Delete false true] (do ; // TODO: should work only on PC, и нужно ли это вообще?
                                                                       (delete-one-word-right context)
                                                                       (in-key-buffer-clear context) true)
          [com.googlecode.lanterna.input.Key$Kind/Home false false] (do
                                                                      (move-cursor-on-bof context)
                                                                      (in-key-buffer-clear context) true)
          [com.googlecode.lanterna.input.Key$Kind/End false false] (do
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
            false)
          (if (.equals f (Key. com.googlecode.lanterna.input.Key$Kind/Escape)) ; processing Escape-sequence
            (condp = s
              ; Delete one word (only for Mac: Escape+Backspace)
              (Key. com.googlecode.lanterna.input.Key$Kind/Backspace) (do ; TODO: should be checked
                                                                        (delete-one-word-left context)
                                                                        (in-key-buffer-clear context) true)
              (Key. com.googlecode.lanterna.input.Key$Kind/Escape) (do ; reset buffer
                                                                     (in-key-buffer-clear context) true)
              false)))
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
