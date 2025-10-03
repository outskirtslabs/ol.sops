;; Copyright © 2025 Casey Link <casey@outskirtslabs.com>
;; SPDX-License-Identifier: MIT
(ns user
  (:require
   [babashka.process :as p]
   [clojure.java.io :as io]
   [ol.sops :as sops]))

(comment
  (slurp (:out
          @(sops/sops :decrypt ["dev/test.sops.yml"]
                      :env {"SOPS_AGE_KEY_FILE" "dev/keys.txt"})))

  (sops/decrypt-file-to-str "dev/test.sops.yml"
                            {:env {"SOPS_AGE_KEY_FILE" "dev/keys.txt"}})

  (let [plaintext "much: wow\n"
        target "dev/test-encrypt.sops.yml"]
    @(sops/encrypt-to-file target plaintext {:age "age15905pjs5av9nyh8rdt4zrzn7x0mdud20eyf7tsvz63mygvsfhd9sclsh94"}))
  ;;
  )

;; decrypt a sops file to string
(sops/decrypt-file-to-str "dev/test.sops.yml"
                          {:env {"SOPS_AGE_KEY_FILE" "dev/keys.txt"}})
;; => "hello: world\n"

;; encrypt a plaintext to a sfile
@(sops/encrypt-to-file "output.sops.json"
                       (edn->json {:foo "bar"})
                       {:age "age15905pjs5av9nyh8rdt4zrzn7x0mdud20eyf7tsvz63mygvsfhd9sclsh94"})
