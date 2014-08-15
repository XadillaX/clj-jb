(ns clj-jb
  (:import [com.huaban.analysis.jieba WordDictionary JiebaSegmenter SegToken Word]))

(defn load-user-dictionary
  "Load customer dictionary."
  [path]
  (try
    (let [instance (.getInstance WordDictionary)]
      (.loadUserDict instance (clojure.java.io/file path))
      true)
    (catch Exception e false)))

(defn split
  "Participle a sentence.
     - mode can be :INDEX or :SEARCH."
  ([sentence mode]
   (let [words (.process
                 (JiebaSegmenter.)
                 sentence
                 (mode (:SegMode JiebaSegmenter)))]
     (vec
       (map #(let [word (.word %)]
              {:word (.getToken word)
               :start (.startOffset %)
               :end (.endOffset %)
               :type (.getTokenType word)}) words))))
  ([sentence] (split sentence :INDEX)))

(defn split-all
  "Participle an array of setences.
     - mode can be :INDEX or :SEARCH."
  ([setences mode]
   (vec (map #(split % mode) setences)))
  ([setences]
   (split-all setences :INDEX)))
