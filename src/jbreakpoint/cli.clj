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
  (assoc context :buffer (conj (context :buffer) in-key)))

(defn input-loop [screen context]
  (while (complement (@context :exit-flag))
    (do
      (def in-key (.readInput screen))
      (if (not= in-key nil)
        (do
          (swap! context buffer-append in-key)
          (.putCharacter (.getTerminal screen) (.getCharacter in-key))
          (if (=
                \q
                (.getCharacter in-key))
            (swap! context #(assoc %1 :exit-flag %2) true))))
      (if (.resizePending screen)
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
