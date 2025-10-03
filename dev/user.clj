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
    @(sops/encrypt-to-file plaintext target
                           {:age "age15905pjs5av9nyh8rdt4zrzn7x0mdud20eyf7tsvz63mygvsfhd9sclsh94"}))
  ;;
  )
