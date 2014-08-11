(ns clj-jb
  (:import [com.huaban.analysis.jieba JiebaSegmenter SegToken Word]))

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
