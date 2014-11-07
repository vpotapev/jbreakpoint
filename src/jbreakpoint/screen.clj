(ns
  ^{:author rhub}
  jbreakpoint.screen
  (:require [clojure.walk :as w])
  (:import (java.nio.charset Charset)
           (com.googlecode.lanterna TerminalFacade)
           (com.googlecode.lanterna.gui Action GUIScreen Window Component)
           (com.googlecode.lanterna.gui.component Button Label Panel Table)
           (com.googlecode.lanterna.gui.layout LinearLayout VerticalLayout)
           (com.googlecode.lanterna.terminal TerminalSize)
           (com.googlecode.lanterna.terminal.text UnixTerminal)))

(defn create-button-action
  [f & args]
  (proxy [Action] []
    (doAction [] (apply f args))))

(defn create-screen [context input-loop]
  (do
    (def screen (TerminalFacade/createScreen (TerminalFacade/createUnixTerminal)))
    (def gui-screen (TerminalFacade/createGUIScreen screen))
;    (def btn (Button. "Exit" (create-button-action #(.close win))))
;    (.setAlignment btn com.googlecode.lanterna.gui.Component$Alignment/RIGHT_CENTER)
    (.startScreen screen)
    (input-loop screen context)
    (.stopScreen screen)))

(defn get-screen-size [screen-class]
  {
    :width (.getColumns (.getTerminalSize screen-class))
    :height (.getRows (.getTerminalSize screen-class))
    })

;TODO: make layout: console window into main window. It needed to simulate jdb console

(defn- iterate-children [id parent-id parent-list]
  (first
    (remove nil?
      (map
        #(if (= (% :id) id)
         (identity parent-id)
         (iterate-children id (% :id) (% :children)))
        parent-list)
      )))

(defn get-parent-wnd [layout wnd-id]
  (iterate-children wnd-id nil layout))

(defn get-wnd-layout [layout wnd-id]
  (first
    (filter
      #(= (% :id) wnd-id)
      (tree-seq #(complement (nil? (% :children))) #(% :children) (first layout)))))

; graphic primitives
;(defn draw-line-vert [screen x1 y1 len]
;  (for [n (range 0 (- len 1))]
;    (.putString screen x1 n "|" com.googlecode.lanterna.terminal.Terminal$Color/WHITE com.googlecode.lanterna.terminal.Terminal$Color/BLACK 0)))
;
;(defn draw-line-horiz [screen x1 y1 len]
;  (.putString screen x1 y1 (take len (repeat "-")) com.googlecode.lanterna.terminal.Terminal$Color/WHITE com.googlecode.lanterna.terminal.Terminal$Color/BLACK 0))
;
;(defn update-screen [screen width height]
;  (let [layout (get-wnd-layout width height)]
;    ))
