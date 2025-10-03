(ns ol.sops.impl-test
  (:require
   [clojure.string :as str]
   [clojure.test :refer [deftest is testing]]
   [ol.sops.impl :as sops]))

(def test-recipient "age16ptz40qvt6sl89f8hm0zlhext42lv0qa22a9mn7f69a0llvzu3hqylekcg")
(def test-key-file-path "./test/fixtures/keys.txt")

(deftest prepare-cmd-map-test
  (testing "command only"
    (is (= {:cmd :help :args []}
           (sops/prepare-cmd-map :help))))

  (testing "command with args"
    (is (= {:cmd :encrypt :args ["file.yaml"]}
           (sops/prepare-cmd-map :encrypt ["file.yaml"]))))

  (testing "command with opts"
    (is (= {:cmd :encrypt :args [] :opts {:in-place true}}
           (sops/prepare-cmd-map :encrypt [] :in-place true))))

  (testing "command with args and opts"
    (is (= {:cmd :decrypt :args ["file.yaml"] :opts {:output "out.txt"}}
           (sops/prepare-cmd-map :decrypt ["file.yaml"] :output "out.txt"))))

  (testing "command as map passed through"
    (is (= {:cmd :decrypt :args ["file.yaml"]}
           (sops/prepare-cmd-map {:cmd :decrypt :args ["file.yaml"]}))))

  (testing "opts with :in key"
    (is (= {:cmd :encrypt :args [] :in "stdin-data" :opts {:input-type "json"}}
           (sops/prepare-cmd-map :encrypt :in "stdin-data" :input-type "json"))))

  (testing "opts with :as key"
    (is (= {:cmd :decrypt :args [] :as :bool :opts {}}
           (sops/prepare-cmd-map :decrypt :as :bool))))

  (testing "multi-opt handling"
    (is (= {:cmd :encrypt :args [] :opts {:age ["key1" "key2"]}}
           (sops/prepare-cmd-map :encrypt :age ["key1" "key2"])))))

(deftest decrypt-encrypted-fixture-test
  (testing "decrypt fixture using age key"
    (let [fixture-path "test/fixtures/encrypted.sops.yml"
          key-file-path "./test/fixtures/keys.txt"
          {:keys [exit out]} (binding [sops/*process-opts* {:extra-env {"SOPS_AGE_KEY_FILE" key-file-path}}]
                               @(sops/sops :decrypt [fixture-path]
                                           :out :string))]
      (is (= 0 exit))
      (is (= "password: hunter2\n" out)))))

(deftest json-round-trip
  (testing "encrypt and decrypt JSON from stdin"
    (let [json-input              "{\"secret\":\"my-password\"}"
          {:keys [out exit]} @(sops/sops :encrypt
                                         []
                                         :in json-input
                                         :input-type "json"
                                         :filename-override "secret.json"
                                         :out :string
                                         :age test-recipient)
          ciphertext              out]
      (is (= 0 exit))
      (is (str/includes? ciphertext "\"secret\": \"ENC["))
      (let [{:keys [exit out]} @(sops/sops :decrypt
                                           :in ciphertext
                                           :input-type "json"
                                           :output-type "json"
                                           :env {"SOPS_AGE_KEY_FILE" test-key-file-path})]
        (is (= 0 exit))
        (is (str/includes? (slurp out) "\"secret\": \"my-password\""))))))
