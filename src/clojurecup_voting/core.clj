(ns clojurecup-voting.core
  (:require
   [clj-http.client :as client]
   [clj-time.core :as t]
   [clj-time.coerce :as tc]
   [clj-time.format :as tf]
   [cheshire.core :as json]
   [clojure.core.async :as async :refer [chan <! >! <!! timeout go go-loop]])
  (:gen-class))

(def teams-url "https://backend.clojurecup.com/teams")
(def data-store (atom nil))
(def dump-filename "favs.dump")
(def delay-seconds (* 5 60))

(defn now-ts-rounded []
  (-> (t/now)
      (tc/to-long)
      (quot (* delay-seconds 1000))
      (* delay-seconds 1000)))

(defn fetch-data []
  (as-> teams-url x
      (client/get x {:as :transit+json})
      (:body x)
      (map (juxt :team/app-name :faver-count) x)
      (assoc {:timestamp (now-ts-rounded)} :data x)))

(defn merge-store [store fetched]
  (let [timestamps (get store :timestamps [])
        teams (get store :teams {})
        new-timestamp (:timestamp fetched)
        new-data (:data fetched)]
    {:timestamps (conj timestamps new-timestamp)
     :teams (reduce (fn [acc [team-name favs]]
                      (if (get acc team-name)
                        (update-in acc [team-name] conj favs)
                        (assoc acc team-name [favs])))
                    teams
                    new-data)}))

(defn dump-data-store []
  (spit dump-filename (json/encode @data-store)))

(defn make-update []
  (let [new-data (fetch-data)]
    (swap! data-store merge-store new-data)
    (dump-data-store)))

(defn main-loop []
  (go-loop []
    (let [start (. System (nanoTime))]
      (println (tf/unparse (tf/formatters :date-time) (t/now)) "updating...")
      (make-update)
      (println (tf/unparse (tf/formatters :date-time) (t/now)) "updated")
      (<! (timeout (- (* delay-seconds 1000)
                      (int (/ (double (- (. System (nanoTime)) start)) 1000000.)))))
      (recur))))

(defn -main [& args]
  (try (let [dump-str (slurp dump-filename)
             raw-dump (json/decode dump-str)
             keywordized (into {} (map (fn [[k v]] [(keyword k) v]) raw-dump))]
         (reset! data-store keywordized))
       (catch java.io.FileNotFoundException e
         :ok))
  (println "starting main loop...")
  (<!! (main-loop)))
