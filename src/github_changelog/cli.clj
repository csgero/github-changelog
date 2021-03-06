(ns github-changelog.cli
  (:gen-class)
  (:require [clojure
             [edn :as edn]
             [string :as str]]
            [clojure.tools.cli :refer [parse-opts]]
            [github-changelog.core :refer [changelog]]
            [github-changelog.formatters.markdown :refer [format-tags]]))

(def cli-options
  [["-l" "--last LAST" "Generate changes only for the last n tags"
     :parse-fn #(Integer/parseInt %)
     :validate [#(< 0 %) "Must be a positive number"]]
    ["-h" "--help"]])

(defn- join-lines [lines]
  (str/join \newline (flatten lines)))

(defn- exit [status msg]
  (when (seq msg)
    (println msg))
  (System/exit status))

(defn- error-msg [errors]
  (join-lines ["The following errors occurred while parsing your command:"
               ""
               errors]))

(defn- read-config [file]
  (edn/read-string (slurp file)))

(defn- generate [file last]
  (let [all-tags (changelog (read-config file))
        tags (if last (take last all-tags) all-tags)]
    (format-tags tags)))

(defn- usage [options-summary]
  (join-lines ["Usage: program-name [options] <config.edn> ..."
               ""
               "Options:"
               options-summary]))

(defn -main [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)
        {:keys [help last]} options]
    (cond
      help (exit 0 (usage summary))
      (empty? arguments) (exit 1 (usage summary))
      errors (exit 1 (error-msg errors)))

    (doseq [config-file arguments]
      (println (generate config-file last)))

    (exit 0 nil)))
