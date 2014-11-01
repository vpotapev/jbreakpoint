(ns jbreakpoint.screen-test
  (:require ;;[clojure.test :refer :all]
            [jbreakpoint.screen :as s])
  (:use [expectations])
  (import (com.googlecode.lanterna.input Key)))

; test screen layout
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
             )]
  (do
    (s/)))
