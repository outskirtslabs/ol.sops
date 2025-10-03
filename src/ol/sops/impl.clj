;; Copyright © 2023- Rahul De. (@lispyclouds)
;; SPDX-License-Identifier: MIT
;; Based on the babashka process wrapper from https://github.com/lispyclouds/bblgum
(ns ^:no-doc ol.sops.impl
  "Not public"
  (:require
   [babashka.process :as p]
   [clojure.string :as str]))

(def ^:dynamic *process-opts* nil)

(defn ->str
  [thing]
  (if (keyword? thing)
    (name thing)
    thing))

(defn multi-opt
  [opt]
  (if (sequential? opt)
    (str/join "," opt)
    opt))

(defn exec
  [cmd in-stream out-stream env]
  (let [opts (-> {:out out-stream
                  :in in-stream
                  :extra-env env
                  :err :string
                  :continue true
                  :shutdown p/destroy-tree}
                 (merge *process-opts*))]
    (apply p/process opts cmd)))

(defn run
  [{:keys [cmd opts args in sops-path env out]}]
  (when-not cmd
    (throw (IllegalArgumentException. ":cmd must be provided or non-nil")))
  (let [sops-path (or sops-path "sops")
        with-opts (->> opts
                       (map (fn [[opt value]]
                              (str "--" (->str opt) "=" (multi-opt value))))
                       (into [sops-path (->str cmd)]))
        args (if (or (empty? args) (= "--" (first args)))
               args
               (cons "--" args))]
    (exec (into with-opts args) in out env)))

(defn prepare-options
  [m]
  (let [fmted-keys [:opts :in :out :as :sops-path :env]
        fmted (select-keys m fmted-keys)
        opts (apply dissoc m fmted-keys)]
    (update fmted :opts merge opts)))

(defn prepare-cmd-map
  "Prepares command map to be passed to `run`. Tries to be smart and figure out what user wants."
  ([cmd]
   (if (map? cmd)
     cmd
     (prepare-cmd-map cmd [] nil)))
  ([cmd args-or-opts]
   (if (sequential? args-or-opts)
     (prepare-cmd-map cmd args-or-opts nil)
     (prepare-cmd-map cmd [] args-or-opts)))
  ([cmd args & options]
   (let [args* (if (sequential? args) args [])
         foptions (filter some? options)
         options* (if (keyword? args) (conj foptions args) foptions)
         options-map (cond
                       (empty? options*) {}
                       (and (= 1 (count options*)) (map? (first options*))) (first options*)
                       :else (apply hash-map options*))]
     (merge {:cmd cmd :args args*} (if (seq options-map) (prepare-options options-map) {})))))

(defn sops
  ([cmd]
   (run (prepare-cmd-map cmd)))
  ([cmd args-or-opts]
   (run (prepare-cmd-map cmd args-or-opts)))
  ([cmd args & opts]
   (run (apply prepare-cmd-map cmd args opts))))

(defn decrypt-file-to-str [file opts]
  (:out (p/check (apply sops :decrypt [file] (apply concat (merge opts {:out :string}))))))

(defn encrypt-to-file [plaintext file opts]
  (binding [*process-opts* {:out :write :out-file file}]
    (let [merged-opts (merge opts {:in plaintext :filename-override file})
          proc (apply sops :encrypt [] (apply concat merged-opts))]
      (p/check proc))))

(comment
  (prepare-cmd-map :help) ;; => {:cmd :sop :args []}
  (prepare-cmd-map :encrypt :in-place true) ;; => {:cmd :encrypt, :args [], :opts {:in-place true}}
  )
