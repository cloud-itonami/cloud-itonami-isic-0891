(ns chemmineops.facts-test
  (:require [clojure.test :refer [deftest is]]
            [chemmineops.facts :as facts]))

(deftest jpn-has-a-spec-basis
  (is (some? (facts/spec-basis "JPN")))
  (is (string? (:provenance (facts/spec-basis "JPN")))))

(deftest all-four-seeded-jurisdictions-have-a-blast-spec-basis
  ;; unlike some prior repair-shop-cluster siblings' own honest single-
  ;; jurisdiction gap, ALL FOUR seeded jurisdictions actually have a
  ;; real explosives/blast-safety enforcement regime here -- reported
  ;; honestly, not forced narrower
  (doseq [iso3 ["JPN" "USA" "GBR" "DEU"]]
    (is (some? (facts/blast-spec-basis iso3)) (str iso3 " blast-spec-basis"))
    (is (string? (:blast-provenance (facts/blast-spec-basis iso3))) (str iso3 " blast-provenance"))))

(deftest unknown-jurisdiction-has-no-fabricated-spec-basis
  (is (nil? (facts/spec-basis "ATL"))))

(deftest unknown-jurisdiction-has-no-blast-spec-basis
  (is (nil? (facts/blast-spec-basis "ATL"))))

(deftest coverage-never-reports-a-missing-jurisdiction-as-covered
  (let [report (facts/coverage ["JPN" "ATL" "GBR"])]
    (is (= 2 (:covered report)))
    (is (= ["ATL"] (:missing-jurisdictions report)))
    (is (= ["GBR" "JPN"] (:covered-jurisdictions report)))))

(deftest required-evidence-satisfied-needs-every-item
  (let [all (facts/evidence-checklist "JPN")]
    (is (facts/required-evidence-satisfied? "JPN" all))
    (is (not (facts/required-evidence-satisfied? "JPN" (rest all))))
    (is (not (facts/required-evidence-satisfied? "ATL" all)) "no spec-basis -> never satisfied")))
