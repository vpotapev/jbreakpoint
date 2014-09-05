(ns
  ^{:author rhub}
  jcdb.cli
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

(defn create-main-window []
  (let [main-window (Window. "JCDB")
        panel-holder (Panel. "WindowPanel" com.googlecode.lanterna.gui.component.Panel$Orientation/VERTICAL)
        menu-button-list (map #(Button. %) ["File" "Edit" "Debug" "About"])
        menu-panel (Panel. "MainMenu")]
    (do
      (.setWindowSizeOverride main-window (TerminalSize. 80 40))
      (.setSoloWindow main-window true)
      (map #(.setAlignment % com.googlecode.lanterna.gui.Component$Alignment/RIGHT_CENTER) menu-button-list)
      (.setLayoutManager menu-panel (VerticalLayout.))
      (map #(.addComponent menu-panel % LinearLayout/GROWS_HORIZONTALLY) menu-button-list))
    main-window))

(defn create-screen []
  (do
    (def win (create-main-window))
    (def screen (TerminalFacade/createScreen (TerminalFacade/createUnixTerminal)))
    (def gui-screen (TerminalFacade/createGUIScreen screen))
    (def btn (Button. "Exit" (create-button-action #(.close win))))
    (.setAlignment btn com.googlecode.lanterna.gui.Component$Alignment/RIGHT_CENTER)
    (.addComponent win btn (into-array [LinearLayout/GROWS_HORIZONTALLY]))
    (.startScreen screen)
    (.showWindow gui-screen win)
    (.stopScreen screen)))
