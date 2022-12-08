(ns build
  (:require [clojure.tools.build.api :as b]
            [clojure.edn :as edn])
  (:refer-clojure :exclude [compile]))

(def deps-data (edn/read-string (slurp "deps.edn")))
(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def jar-file (format "target/difftest.jar"))
(def uber-file (format "target/uber-difftest.jar"))

(defn clean [_]
  (b/delete {:path "target"}))

(defn compile [_]
  (b/javac {:src-dirs ["java"]
            :class-dir class-dir
            :basis basis
            :javac-opts ["-source" "8" "-target" "8" "-Xlint:unchecked"
                         ;;"--add-modules" "jdk.incubator.vector"
                         ]}))


(defn uberjar [arg]
  (compile arg)
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'difftest.diffs})
  )
