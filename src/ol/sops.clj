;; Copyright © 2025 Casey Link <casey@outskirtslabs.com>
;; SPDX-License-Identifier: MIT
(ns ol.sops
  (:require [ol.sops.impl :as impl]))

(defn sops
  "A simple wrapper around babashka.process for calling sops.

  You can call `sops` like this:

  (sops :command [\"args vector\"] :opt \"value\" :opt2 \"value2\")

  There are several special opts, that are handled by ol.sops:
  :in - An input stream than can be passed to sops
  :out - :string if you want to get a string back, nil (the default) will return an output stream you can slurp
  :dir - The working directory. Defaults to the current working directory
  :sops-path - Path to the sops binary. Defaults to `sops`
  :env - a map of environment variables that are passed to the process


  All other opts are passed to the `sops` CLI. Consult `sops CMD --help` to see available options.
  To pass flags like `--verbose` use `:verbose true`. Always use full names of the options, not the short versions like -v.

  Refer to [[babashka.process/process]] for the exact behavior of: :in, :out, :dir, and the exact nature of the return value.

  Returns a record with (among other things, see [[babashka.process/process]]):
   - `:out` the output stream
   - `:exit` the exit code of sops
   - `:err` a string containing the stderr output from sops, if any

  If you [[clojure.core/deref]] the record, then it will block until the process has exited.
  If you slurp from the out stream, it will also block until the process has exited.

  Usage examples
  --------------
  Decrypt file in place
  @(sops :decrypt [\"secrets.sops.yaml\"] :in-place)

  Decrypt file to a string
  (slurp (:out (sops :decrypt [\"secrets.sops.yaml\"])))"
  ([cmd]
   (impl/sops cmd))
  ([cmd args-or-opts]
   (impl/sops cmd args-or-opts))
  ([cmd args & opts]
   (apply impl/sops cmd args opts)))

(defn decrypt-file-to-str
  "Decrypts the path `file` and returns the output as a string. Sugar over [[sops]].
  
  Options:
  - `:input-type` - Override file type detection (yaml, json, env, ini, binary)
  - `:output-type` - Override output format (yaml, json, env, ini, binary)
  - Other SOPS options as documented in `sops decrypt --help`
  
  Example:
  ```clojure
  (decrypt-file \"secrets.sops.yaml\" {})
  (decrypt-file \"secrets.enc\" {:input-type \"yaml\" :output-type \"json\"})
  ```"
  [file opts]
  (impl/decrypt-file-to-str file opts))

(defn encrypt-to-file
  "Encrypts `plaintext` string and writes encrypted content to `file`. Sugar over [[sops]].
  
  The `file` path is used to match creation rules in `.sops.yaml` and determine the file format.
  
  Options:
  - `:age` - Age recipient(s) to encrypt for (can be a single string or vector of strings)
  - `:pgp` - PGP fingerprint(s) to encrypt for (can be a single string or vector of strings)
  - `:gcp-kms` - GCP KMS resource ID(s) to encrypt with (can be a single string or vector of strings)
  - `:azure-kv` - Azure Key Vault URL(s) to encrypt with (can be a single string or vector of strings)
  - `:kms` - AWS KMS ARN(s) to encrypt with (can be a single string or vector of strings)
  - `:aws-profile` - The AWS profile to use for requests to AWS
  - Other SOPS options as documented in `sops encrypt --help`
  
  Example:
  ```clojure
  (encrypt-to-file \"secrets.sops.yaml\" \"foo: bar\"  {:age \"age1...\"})
  ```"
  [file plaintext opts]
  (impl/encrypt-to-file file plaintext opts))

(defmacro with-process-opts
  "An escape hatch that executes body with additional babashka/process options merged into the process call.

  Example:
  ```clojure
  ;; Override the environemnt
  (with-process-opts {:extra-env {:SOPS_AGE_KEY_FILE \"/some/path/to/keys.txt\"}}
    (sops :decrypt [\"secrets.yaml\"]))
  ```"
  [env-map & body]
  `(binding [impl/*process-opts* (merge impl/*process-opts* ~env-map)]
     ~@body))
