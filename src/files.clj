(ns files
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.edn :as edn])
  (:import [java.nio.file Files Paths StandardCopyOption]))

(set! *warn-on-reflection* true)

(defn canonical-path [^java.io.File file]
  (.getCanonicalPath file))

(defn file-path->name
  "Gets file name from the file path and changes file extension (e.g. `html`) if provided"
  ([path extension]
   (let [file-name (-> (io/file path)
                       (.getName))]
     (if extension
       (str/replace file-name #"\.[^.]+$" (str "." extension))
       file-name)))
  ([path]
   (file-path->name path nil)))

(defn make-parents-dirs
  [file-path]
  (io/make-parents file-path))

(defn copy-file [source-path dest-path]
  (let [dest-abs-path (-> (io/file dest-path)
                          (.getAbsolutePath))
        file-name (-> (io/file source-path)
                      (.getName))
        dest-file-path (str dest-abs-path "/" file-name)
        dest-file (io/file dest-file-path)]
    (.mkdirs (.getParentFile dest-file))
    (io/copy (io/file source-path) dest-file)))

(defn delete-file
  [file-path]
  (io/delete-file file-path))

(defn list-dir-files
  "List files in the given directory with a prefix in the filename"
  ([dir-path]
   (list-dir-files dir-path nil nil))
  ([dir-path prefix recursive?]
   (let [dir (io/file dir-path)
         files (if recursive? (file-seq dir) (.listFiles dir))]
     (when (.isDirectory dir)
       (->> files
            (filter #(.isFile ^java.io.File %))
            (#(cond->> %
                prefix (filter (fn [file]
                                 (str/starts-with? (.getName ^java.io.File file) prefix)))))
            (map (fn [file]
                   (canonical-path file))))))))

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source]
  (try
    (with-open [r (io/reader source)]
      (edn/read (java.io.PushbackReader. r)))

    (catch java.io.IOException e
      (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
    (catch RuntimeException e
      (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))


