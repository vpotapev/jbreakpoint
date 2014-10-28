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
    (binding [*screen* screen])
    (def gui-screen (TerminalFacade/createGUIScreen screen))
;    (def btn (Button. "Exit" (create-button-action #(.close win))))
;    (.setAlignment btn com.googlecode.lanterna.gui.Component$Alignment/RIGHT_CENTER)
    (.startScreen screen)
    (input-loop screen context)
    (.stopScreen screen)))

;TODO: make layout: console window into main window. It needed to simulate jdb console

(binding [*screen-layout*
          '(
             {:id :main-screen-wnd
              :left 0
              :top 0
              :width (.getColumns (.getTerminalSize *screen*))
              :height (.getRows (.getTerminalSize *screen*))
              :children '(
                           {:id :wnd-console
                            :left 0
                            :top 0
                            :width ((get-wnd-layout :main-screen-wnd screen) :width)
                            :height ((get-wnd-layout :main-screen-wnd screen) :height)}
                           )}
             )])

(defn get-wnd-layout [wnd-id screen]
  (let [layout *screen-layout*]
    (layout wnd-id)))

(defn draw-line-vert [screen x1 y1 len]
  (for [n (range 0 (- len 1))]
    (.putString screen x1 n "|" com.googlecode.lanterna.terminal.Terminal$Color/WHITE com.googlecode.lanterna.terminal.Terminal$Color/BLACK 0)))

(defn draw-line-horiz [screen x1 y1 len]
  (.putString screen x1 y1 (take len (repeat "-")) com.googlecode.lanterna.terminal.Terminal$Color/WHITE com.googlecode.lanterna.terminal.Terminal$Color/BLACK 0))

(defn update-screen [screen width height]
  (let [layout (get-wnd-layout width height)]
    ))
