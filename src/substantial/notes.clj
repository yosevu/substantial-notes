(ns substantial.notes
  (:require [clojure.java.io :as io]
            [clojure.string :as string]
            [markdown.core :refer [md-to-html-string-with-meta]]))

(def site-url "https://notes.yosevu.com/")
(def default-notes-path "notes")

(defn slurp-dir [path]
  (map slurp (rest (file-seq (io/file path)))))

;; FIXME md to html not working
;; (def backlink-match-1 #"(.*)(\]\()([a-z0-9|-]+)(.*)")
(def backlink-match #"(\]\()([a-z0-9|-]+)")

;; (defn backlink-replacement-1 [site-url]
;;   (str "$1" "$2" site-url "$3" ".html" "$4"))

(defn backlink-replacement [site-url]
  (str "$1" site-url "$2" ".html"))

(defn add-backlink [backlink-text state]
  [(string/replace backlink-text backlink-match
                   (backlink-replacement site-url)) state])
(defn md-to-html-with-backlinks
  [filestring]
  (md-to-html-string-with-meta
    filestring
    :replacement-transformers
    [add-backlink]))

(defn md->html [filestrings]
  (map md-to-html-with-backlinks filestrings))
  
(defn sort-by-date [notes]
  (reverse (sort-by #(get-in (:metadata %) [:date]) notes)))

(defn into-map [notes-map note]
  (assoc notes-map (keyword (first (:slug (:metadata note)))) note))

(defn get-notes
  ([] (get-notes default-notes-path))
  ([notes-path]
   (reduce into-map {} (sort-by-date (md->html (slurp-dir notes-path))))))

(defn get-note [slug]
  ((keyword slug) (get-notes)))

(comment
  (def backlink-text "abc[Today I learned](learning-journal)123")

  (string/replace backlink-text backlink-match
                  (backlink-replacement site-url))
  ;; => "[Today I learned](https://notes.yosevu.com/learning-journal.html)"

  (md-to-html-string-with-meta
   backlink-text
   :replacement-transformers
   [add-backlink]) 
  )
