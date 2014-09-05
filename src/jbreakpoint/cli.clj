(ns
  ^{:author rhub}
  jbreakpoint.cli
  (:import (java.nio.charset Charset)
           (com.googlecode.lanterna TerminalFacade)
           (com.googlecode.lanterna.gui Action GUIScreen Window Component)
           (com.googlecode.lanterna.gui.component Button Label Panel Table)
           (com.googlecode.lanterna.gui.layout LinearLayout VerticalLayout)
           (com.googlecode.lanterna.terminal TerminalSize)
           (com.googlecode.lanterna.terminal.text UnixTerminal)))

(def event-queue)

(defn buffer-append [key]
  )

(defn input-loop [screen context]
  (def exit-flag (atom false))
  (while @exit-flag
    (do
      (def key (.readInput screen))
      (if (not= key nil)
        (swap! context buffer-append key))
      (if (.resizePending screen)
        (.refresh screen)))))
