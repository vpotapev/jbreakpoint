(ns jbreakpoint.screen-test
  (:require [jbreakpoint.screen :as s])
  (:use [expectations])
  (import (com.googlecode.lanterna.input Key)))

; screen layout defined as function
(def testlayout
  '(
     {:id :main-screen-wnd
      :left 0
      :top 0
      :width {:parent :width}
      :height {:parent :height}
      :children (
                  {:id :wnd-console
                  :left 0
                  :top 0
                  :width {:parent :width}
                  :height {:parent :height}
                  :children (
                              {:id :wnd-console-inner
                               :left 0
                               :top 0
                               :width {:parent :width}
                               :height {:parent :height}
                               })
                  })
      })
  )

; get parent of a specified window
(expect
  :main-screen-wnd
  (s/get-parent-wnd testlayout :wnd-console))
(expect
  :wnd-console
  (s/get-parent-wnd testlayout :wnd-console-inner))
(expect
  nil
  (s/get-parent-wnd testlayout :main-screen-wnd))
(expect
  nil
  (s/get-parent-wnd testlayout :fake-window))

; get layout of a specified window
(expect
  {:id :wnd-console-inner
   :left 0
   :top 0
   :width {:parent :width}
   :height {:parent :height}
   }
  (s/get-wnd-layout testlayout :wnd-console-inner))
(expect
  (first testlayout)
  (s/get-wnd-layout testlayout :main-screen-wnd))
(expect
  nil
  (s/get-wnd-layout testlayout :fake-window))
