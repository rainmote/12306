(defproject rainmote "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[baking-soda "0.2.0" :exclusions [cljsjs/react-bootstrap]]
                 [buddy "2.0.0"]
                 [ch.qos.logback/logback-classic "1.2.3"]
                 [clj-time "0.14.4"]
                 [cljs-ajax "0.7.4"]
                 [cljsjs/react-popper "0.10.4-0"]
                 [cljsjs/react-transition-group "2.4.0-0"]
                 [com.cognitect/transit-java "0.8.337"]
                 [com.fasterxml.jackson.core/jackson-core "2.9.6"]
                 [com.fasterxml.jackson.datatype/jackson-datatype-joda "2.9.6"]
                 [com.walmartlabs/lacinia "0.28.0"]
                 [compojure "1.6.1"]
                 [conman "0.8.2"]
                 [cprop "0.1.11"]
                 [funcool/struct "1.3.0"]
                 [luminus-aleph "0.1.5"]
                 [luminus-migrations "0.5.3"]
                 [luminus/ring-ttl-session "0.3.2"]
                 [markdown-clj "1.0.2"]
                 [metosin/compojure-api "1.1.12"]
                 [metosin/muuntaja "0.6.0"]
                 [metosin/ring-http-response "0.9.0"]
                 [mount "0.1.13"]
                 [nrepl "0.4.5"]
                 [org.clojure/clojure "1.9.0"]
                 [org.clojure/clojurescript "1.10.339" :scope "provided"]
                 [org.clojure/tools.cli "0.3.7"]
                 [org.clojure/tools.logging "0.4.1"]
                 [org.postgresql/postgresql "42.2.4"]
                 [org.webjars.bower/tether "1.4.4"]
                 [org.webjars/bootstrap "4.1.3"]
                 [org.webjars/font-awesome "5.3.1"]
                 [org.webjars/webjars-locator "0.34"]
                 [re-frame "0.10.6"]
                 [reagent "0.8.1"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.6.3"]
                 [ring/ring-defaults "0.3.2"]
                 [secretary "1.2.3"]
                 [selmer "1.12.0"]

                 [com.taoensso/sente "1.13.1"]
                 [com.taoensso/timbre "4.10.0"]
                 [antizer "0.3.1"]
                 [clj-antd-pro "0.1.0"]
                 ]
  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"
                   "/Users/lianshitw/clj-antd-pro/target/clj-antd-pro-0.1.0.jar"]
  :target-path "target/%s/"
  :main ^:skip-aot rainmote.core

  :plugins [[lein-cljsbuild "1.1.7"]
            [lein-sassc "0.10.4"]
            [lein-auto "0.1.2"]
            [lein-kibit "0.1.2"]]
   :sassc
   [{:src "resources/scss/screen.scss"
     :output-to "resources/public/css/screen.css"
     :style "nested"
     :import-path "resources/scss"}] 
  
   :auto
   {"sassc" {:file-pattern #"\.(scss|sass)$" :paths ["resources/scss"]}} 
  
  :hooks [leiningen.sassc]
  :clean-targets ^{:protect false}
  [:target-path [:cljsbuild :builds :app :compiler :output-dir] [:cljsbuild :builds :app :compiler :output-to]]
  :figwheel
  {:http-server-root "public"
   :nrepl-port 7002
   :css-dirs ["resources/public/css"]
   :nrepl-middleware
   [cider/wrap-cljs-repl cider.piggieback/wrap-cljs-repl]}
  

  :profiles
  {:uberjar {:omit-source true
             :prep-tasks ["compile" ["cljsbuild" "once" "min"]]
             :cljsbuild
             {:builds
              {:min
               {:source-paths ["src/cljc" "src/cljs" "env/prod/cljs"]
                :compiler
                {:output-dir "target/cljsbuild/public/js"
                 :output-to "target/cljsbuild/public/js/app.js"
                 :source-map "target/cljsbuild/public/js/app.js.map"
                 :optimizations :advanced
                 :pretty-print false
                 :closure-warnings
                 {:externs-validation :off :non-standard-jsdoc :off}
                 :externs ["react/externs/react.js"]}}}}
             
             
             :aot :all
             :uberjar-name "rainmote.jar"
             :source-paths ["env/prod/clj"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[binaryage/devtools "0.9.10"]
                                 [cider/piggieback "0.3.9"]
                                 [day8.re-frame/re-frame-10x "0.3.3-react16"]
                                 [doo "0.1.10"]
                                 [expound "0.7.1"]
                                 [figwheel-sidecar "0.5.16"]
                                 [pjstadig/humane-test-output "0.8.3"]
                                 [prone "1.6.0"]
                                 [ring/ring-devel "1.6.3"]
                                 [ring/ring-mock "0.3.2"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.23.0"]
                                 [lein-doo "0.1.10"]
                                 [lein-figwheel "0.5.16"]]
                  :cljsbuild
                  {:builds
                   {:app
                    {:source-paths ["src/cljs" "src/cljc" "env/dev/cljs"]
                     :figwheel {:on-jsload "rainmote.core/mount-components"}
                     :compiler
                     {:main "rainmote.app"
                      :asset-path "/js/out"
                      :output-to "target/cljsbuild/public/js/app.js"
                      :output-dir "target/cljsbuild/public/js/out"
                      :source-map true
                      :optimizations :none
                      :pretty-print true
                      :closure-defines {"re_frame.trace.trace_enabled_QMARK_" true}
                      :preloads [day8.re-frame-10x.preload]}}}}
                  
                  
                  
                  :doo {:build "test"}
                  :source-paths ["env/dev/clj"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]
                  :cljsbuild
                  {:builds
                   {:test
                    {:source-paths ["src/cljc" "src/cljs" "test/cljs"]
                     :compiler
                     {:output-to "target/test.js"
                      :main "rainmote.doo-runner"
                      :optimizations :whitespace
                      :pretty-print true}}}}
                  
                  }
   :profiles/dev {}
   :profiles/test {}})
