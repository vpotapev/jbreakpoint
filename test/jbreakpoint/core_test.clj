(ns jbreakpoint.core-test
  (:use ;;[clojure.test :refer :all]
        ;;[jbreakpoint.core :refer :all]
        [expectations]))

(expect 2 (+ 1 1))
