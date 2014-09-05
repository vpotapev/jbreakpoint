(ns jcdb.cli-test
  (:use ;;[clojure.test :refer :all]
        [jcdb.cli :as c]
        [expectations]))

(expect ((complement nil?) c/get-screen))
