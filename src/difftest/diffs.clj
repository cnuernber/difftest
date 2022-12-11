(ns difftest.diffs
  (:require [ham-fisted.api :as hamf]
            [tech.v3.dataset :as ds]
            [tech.v3.datatype :as dtype]
            [criterium.core :as crit]
            [uncomplicate.neanderthal
             [core :as nc]
             [native :refer [dv] :as nn]
             [real :as nr]
             [random :refer [rand-normal! rand-uniform! rng-state]]])
  (:import [difftest Diff1D
            ;;Diff1DVecOps
            ])
  (:gen-class))

(set! *unchecked-math* :warn-on-boxed)


(def some-data (ds/->dataset {:x [12 123 23 45 45 1 123 5 1]}))
(def big-data  (ds/->dataset {:x (range 100000)}))

(def x (hamf/double-array (some-data :x)))
(def big-x (hamf/double-array (big-data :x)))

(def n-x (dv (hamf/->random-access x)))
(def n-big-x (dv (hamf/->random-access big-x)))

(defn array-diff [^doubles col]
  (let [res (double-array (dec (alength col)))]
    (dotimes [idx (alength res)]
      (aset res idx (- (aget col (inc idx)) (aget col idx))))
    res))


(defn java-array-diff [^doubles col]
  (Diff1D/diff1d col))

#_(defn java-vecops-diff [^doubles col]
  (Diff1DVecOps/diff1d col))


(defn neanderthal-diff [origin]
  (let [k (unchecked-dec (nc/dim origin))
        l (nc/subvector origin 0 k)
        r (nc/subvector origin 1 k)]
    (nc/axpy -1 l r)))


(defn dtype-diff
  [data]
  (let [b (dtype/->reader data :float64)]
    (-> (dtype/make-reader
         :float64 (dec (.lsize b))
         (- (.readDouble b (unchecked-inc idx))
            (.readDouble b idx)))
        (dtype/clone))))

(defn -main [& args]
  (println "array-diff")
  (crit/quick-bench (array-diff x))
  (println "java-array-diff")
  (crit/quick-bench (java-array-diff x))
  (println "neanderthal")
  (crit/quick-bench (neanderthal-diff n-x))
  (println "dtype-diff")
  (crit/quick-bench (dtype-diff x))

  (println "big array-diff")
  (crit/quick-bench (array-diff big-x))
  (println "big java-array-diff")
  (crit/quick-bench (java-array-diff big-x))
  (println "big neanderthal")
  (crit/quick-bench (neanderthal-diff n-big-x))
  (println "big dtype-diff")
  (crit/quick-bench (dtype-diff big-x))
  (println "done!!")
  (shutdown-agents))


(defn sq-ones-raw[n]
   (let [init (nn/fv n)
         x (nr/entry! init 1)]
       (nc/rk init init)))

(def sq-ones (memoize sq-ones-raw))

(defn sqm [n v]
  (nc/scal! v (sq-ones n)))


(comment

  (crit/quick-bench (array-diff x))
  ;; 20ns
  ;; JDK-17 20ns
  (crit/quick-bench (java-array-diff x))
  ;; 17ns
  ;; JDK-17 - 20ns
  (crit/quick-bench (java-vecops-diff x))
  ;; JDK-17 - 12ns
  (crit/quick-bench (neanderthal-diff x))
  ;; 1.9us


  (crit/quick-bench (array-diff big-x))
  ;;159us
  ;;JDK-17 198us
  (crit/quick-bench (java-array-diff big-x))
  ;; 89us
  (crit/quick-bench (java-vecops-diff big-x))

  (crit/quick-bench (neanderthal-diff big-x))
  ;;422 us
  (crit/quick-bench (neanderthal-diff-no-transfer n-big-x))
  ;; 92us

  (crit/quick-bench (vec big-x))
  ;; 1.5ms
  (crit/quick-bench (hamf/vec big-x))
  ;; 392us
  )
