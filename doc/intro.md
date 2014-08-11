Clojure Jieba
======

The Clojure version of Jieba (https://github.com/fxsjy/jieba).

Installation
------

### Leiningen

```clojure
[com.huaban/clj-jb "0.1.0-SNAPSHOT"]
```

### Gradle

```sh
compile "com.huaban:clj-jb:0.1.0-SNAPSHOT"
```

### Maven

```xml
<dependency>
  <groupId>com.huaban</groupId>
  <artifactId>clj-jb</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

Usage
------

Require it first:

```clojure
(ns ...
  (:require [com.huaban/clj :as jb]))

(jb/split "意思意思而已，没什么意思。")
; => [{:word "意思", :start 0, :end 2, :type "n"} {:word "意思", :start 2, :end 4, :type "n"} {:word "而已", :start 4, :end 6, :type "y"} {:word "，", :start 6, :end 7, :type ""} {:word "什么", :start 8, :end 10, :type "r"} {:word "没什么", :start 7, :end 10, :type "l"} {:word "意思", :start 10, :end 12, :type "n"} {:word "。", :start 12, :end 13, :type ""}]

(jb/split-all ["意思意思而已，没什么意思。" "我去花瓣网采集花瓣"])
; => [[{:word "意思", :start 0, :end 2, :type "n"} {:word "意思", :start 2, :end 4, :type "n"} {:word "而已", :start 4, :end 6, :type "y"} {:word "，", :start 6, :end 7, :type ""} {:word "什么", :start 8, :end 10, :type "r"} {:word "没什么", :start 7, :end 10, :type "l"} {:word "意思", :start 10, :end 12, :type "n"} {:word "。", :start 12, :end 13, :type ""}] [{:word "我", :start 0, :end 1, :type "r"} {:word "去", :start 1, :end 2, :type "v"} {:word "花瓣", :start 2, :end 4, :type "n"} {:word "网", :start 4, :end 5, :type "n"} {:word "采集", :start 5, :end 7, :type "v"} {:word "花瓣", :start 7, :end 9, :type "n"}]]
```

Thanks To
------

+ @fxsjy: This guy created the Jieba (original version)[https://github.com/fxsjy/jieba].
+ @piaolingxue: He made the (Java version)[https://github.com/huaban/jieba-analysis] of Jieba which `clj-jb` depended on.

Contribute
------

You're welcome to fork and push requests!

