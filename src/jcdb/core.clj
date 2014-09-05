(ns jcdb.core
  (:gen-class)
  (:use [jcdb.cli]))

(defn test1 [a b]
  )

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (jcdb.cli/create-screen))
