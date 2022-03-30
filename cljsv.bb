(require '[babashka.process :refer [process check]]
         '[clojure.string :as str]
		 '[clojure.java.io :as jio]
		 '[clojure.data.csv :as csv])

(defn git-checkout
  [tag]
  @(process ["git" "checkout" tag] {:out :inherit :err :inherit}))

(defn jvm-classes []
  (->> @(process ["ls" "src/jvm/clojure/lang"] {:out :slurp})
       :out
       jio/reader
       line-seq
       (remove str/blank?)
       (filter #(str/ends-with? % ".java"))
       (map #(str "clojure.lang." (subs % 0 (- (count %) 5))))
       sort))

(defn v->tag
  [v]
  (case v
    "1.0.0" "1.0"
	"1.1.0" "1.1.0"
	"1.2.0" "1.2.0"
	(str "clojure-" v)))

(defn collect-serial-vers
  [clj-ver all-vers]
  (println "\nStarting" clj-ver)
  (git-checkout (v->tag clj-ver))
  (let [cp (str/trim (:out @(process 
                    ["clj" "-Spath" "-Sdeps" 
                     (format "{:deps {org.clojure/clojure {:mvn/version \"%s\"}}}" clj-ver)]
                    {:out :string})))]
	;(println cp)
	(reduce
	  (fn [m c]
	    (let [sv (str/trim (:out @(process ["serialver" "-classpath" cp c] {:out :string})))]
          (println c)
		  (if (str/blank? sv)
		    m
			(do
		      (assoc-in m [c clj-ver] (read-string (second (re-find #"(-?[0-9]+)L;" sv))))))))
	  all-vers (jvm-classes))))

(defn collect-all-vers
  [vs out]
  (let [all (sorted-map)
        data (reduce
               (fn [m v] (collect-serial-vers v m))
               (sorted-map) vs)
        header (cons "Version" vs)
		rows (for [c (keys data)]
               (into [c] (map #(get-in data [c %]) vs)))]
	(with-open [w (jio/writer out)]
      (csv/write-csv w (into [header] rows)))))

(collect-all-vers 
  #_#_["1.8.0"] "1.8.csv"
  ["1.0.0" "1.1.0" "1.2.0" "1.3.0" "1.4.0" "1.5.1" "1.6.0" "1.7.0" "1.8.0" "1.9.0" "1.10.3" "1.11.0"] "serial.csv")
