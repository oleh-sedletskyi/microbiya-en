(ns config
  (:refer-clojure :exclude [read])
  (:require
   [clojure.java.io :as io]
   [clojure.pprint :as pprint]
   [clojure.tools.reader.edn :as edn]
   [malli.core :as malli]
   [malli.error :as malli.error]
   [malli.transform :as malli.transform]
   [malli.util :as malli.util]))

(def Non-Empty-String
  (malli/schema [:string {:min 1}]))

(def Config
  [:map {:closed false}
   [:openai
    [:map
     [:token Non-Empty-String]]]])

(defn ^:private parse-file
  [path]
  (when (.exists (io/file path))
    (->> path
         slurp
         (edn/read-string
          ;; allows having a value like #env "FOO" which will be replaced
          ;; by the value of the environment variable FOO
          {:readers {'config/env (fn [s] (java.lang.System/getenv s))}}))))

(defn parse [config schema]
  (let [schema (malli.util/closed-schema schema)
        config (malli/decode schema config malli.transform/string-transformer)]
    (if (malli/validate schema config)
      config
      (let [cause (malli/explain schema config)]
        (throw
         (ex-info (str "Config invalid\n"
                       (with-out-str
                         (pprint/pprint (malli.error/humanize cause)))) cause))))))

(defn read [source-path schema]
  (if-let [config (parse-file source-path)]
    (parse config schema)
    (throw
     (ex-info (str "Config file missing (" source-path ")") {}))))

