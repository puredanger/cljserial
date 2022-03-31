# What

Use this babashka script to compute the serialver of all `clojure.lang.*` classes across all Clojure versions.

# How

Assumes you have installed: git, clj, java, mvn, bb

```
git clone https://github.com/puredanger/cljserial.git
git clone https://github.com/clojure/clojure.git
cd clojure
bb ../cljserial/cljsv.bb
open serial.csv
```

