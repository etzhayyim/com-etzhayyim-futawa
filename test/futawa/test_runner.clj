(ns futawa.test-runner
  (:require [clojure.java.io :as io] [clojure.test :as test]))
(defn- file-ns [f]
  (with-open [r (java.io.PushbackReader. (io/reader f))]
    (second (read {:read-cond :allow :features #{:clj}} r))))
(defn -main [& _]
  (let [fs (->> (file-seq (io/file "test/futawa"))
                (filter #(.isFile %))
                (filter #(re-find #"\.clj(c)?$" (.getName %)))
                (remove #(= "test_runner.clj" (.getName %)))
                (sort-by str))
        nss (mapv file-ns fs)]
    (doseq [f fs] (load-file (str f)))
    (let [{:keys [fail error]} (apply test/run-tests nss)]
      (shutdown-agents)
      (when (pos? (+ fail error)) (System/exit 1)))))
