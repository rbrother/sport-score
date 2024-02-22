(defproject sports "1.0"

  ;; NOTE: This is *dummy* project-file to satisfy IntelliJ IDEA intellisense needs.
  ;; We are now using Shadow-cljs.edn as the primary first-class project file.

  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.773"
                  :exclusions [com.google.javascript/closure-compiler-unshaded
                               org.clojure/google-closure-library
                               org.clojure/google-closure-library-third-party]]
                 [re-frame "1.4.0"]
                 [garden "1.3.10"]
                 [net.dhleong/spade "1.1.0"]
                 [medley "1.3.0"]
                 [com.andrewmcveigh/cljs-time "0.5.2"]
                 [com.rpl/specter "1.1.4"]
                 [metosin/reagent-dev-tools "1.0.0"]
                 [rm-hull/infix "0.3.3"]
                 [cljs-ajax "0.8.4"]
                 [day8.re-frame/http-fx "0.2.4"]]

  :source-paths ["src"]

  :plugins []

  )
