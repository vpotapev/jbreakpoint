(ns
  ^{:author rhub}
  jbreakpoint.screen
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
    (def btn (Button. "Exit" (create-button-action #(.close win))))
    (.setAlignment btn com.googlecode.lanterna.gui.Component$Alignment/RIGHT_CENTER)
    (.startScreen screen)
    (input-loop screen context)
    (.stopScreen screen)))

;TODO: make layout: console window into main window. It needed to simulate jdb console

(defn get-wnd-layout [wnd-id parent-width parent-height]
  (let [layout (
                 {:type :wnd :id :screen-wnd
                    :left 0 :top 0 :width parent-width :height parent-height}
;                 {:type :wnd :id :wnd-sources
;                    :left 0 :top 0 :right width :bottom #(dec (get-layout :splitter1 :top))}
;                 {:type :hsplitter :id :splitter1
;                    :left 0 :top #(* (/ height 4) 3) :right width :bottom #(* (/ height 4) 3)}
;                 {:type :wnd :id :wnd-composite1
;                    :left 0 :top #(+ (get-wnd-param :splitter1 :top) 1) :right width :bottom height}
                 {:type :wnd :id :wnd-console
                    :left (get-wnd-layout :screen-wnd :left)
                    :top (get-wnd-layout :screen-wnd :top)
                    :width (get-wnd-layout :screen-wnd :right)
                    :height (get-wnd-layout :screen-wnd :bottom)}
;                 {:type :vsplitter :id :splitter2
;                    :left #(/ width 2) :top 0 :right width :bottom #(* (/ height 4) 1)}
;                 {:type :menu :id :wnd-menu1
;                    :left 0 :top (dec width)}
   )]
    layout))

(defn draw-line-vert [screen x1 y1 len]
  (for [n (range 0 (- len 1))]
    (.putString screen x1 n "|" com.googlecode.lanterna.terminal.Terminal$Color/WHITE com.googlecode.lanterna.terminal.Terminal$Color/BLACK 0)))

(defn draw-line-horiz [screen x1 y1 len]
  (.putString screen x1 y1 (take len (repeat "-")) com.googlecode.lanterna.terminal.Terminal$Color/WHITE com.googlecode.lanterna.terminal.Terminal$Color/BLACK 0))

(defn update-screen [screen width height]
  (let [layout (get-wnd-layout width height)]
    ))
