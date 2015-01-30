(ns
  ^{:author rhub}
  jbreakpoint.screen
  (:require [clojure.walk :as w]
            [taoensso.timbre :as timbre])
  (:import (java.nio.charset Charset)
           (com.googlecode.lanterna TerminalFacade)
           (com.googlecode.lanterna.gui Action Window Component)
           (com.googlecode.lanterna.gui.component Button Label Panel Table)
           (com.googlecode.lanterna.gui.layout LinearLayout VerticalLayout)
           (com.googlecode.lanterna.terminal TerminalSize)
           (com.googlecode.lanterna.terminal.text UnixTerminal)))
(timbre/refer-timbre)

;(defn create-button-action
;  [f & args]
;  (proxy [Action] []
;    (doAction [] (apply f args))))

(defn create-screen [context input-loop]
  (def term (TerminalFacade/createUnixTerminal))
  (def screen (TerminalFacade/createScreen term))
  (.setCursorVisible term true)
;    (def btn (Button. "Exit" (create-button-action #(.close win))))
;    (.setAlignment btn com.googlecode.lanterna.gui.Component$Alignment/RIGHT_CENTER)
  (.startScreen screen)
  (input-loop screen context)
  (.stopScreen screen))

(defn get-screen-size [screen-class]
  {
    :width (.getColumns (.getTerminalSize screen-class))
    :height (.getRows (.getTerminalSize screen-class))
    })

; TODO: make layout: console window into main window. It needed to simulate jdb console

(defn add-window [layout parent-id new-wnd]
  ); TODO: should be implemented

(defn- filter-children [id parent-list]
  (let [new-parent-list1 (filter
                           #(not= (% :id) id) parent-list)
        new-parent-list2 (map
                           #(filter-children id (% :children)) new-parent-list1)]
    (trace "new-parent-list2: " new-parent-list2)
    (identity new-parent-list2)))

(defn delete-wnd [layout wnd-id]
  (filter-children wnd-id layout))

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

(defn get-wnd [layout wnd-id]
  (first
    (filter
      #(= (% :id) wnd-id)
      (tree-seq #(complement (nil? (% :children))) #(% :children) (first layout)))))

(defn output-to-wnd [layout wnd-id x y out-str]
  ); TODO: should be implemented

(defn clear-wnd [layout wnd-id]
  ); TODO: should be implemented

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
