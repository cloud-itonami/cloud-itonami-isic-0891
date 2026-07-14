(ns chemmineops.phase-test
  "The phase table as executable tests. The invariant this repo cannot
  regress on: `:extraction/extract`/`:consignment/ship` must NEVER be
  a member of any phase's `:auto` set."
  (:require [clojure.test :refer [deftest is testing]]
            [chemmineops.phase :as phase]))

(deftest extraction-extract-never-auto-at-any-phase
  (testing "structural invariant: no phase, now or in the future entries, auto-commits a real extraction"
    (doseq [[n {:keys [auto]}] phase/phases]
      (is (not (contains? auto :extraction/extract))
          (str "phase " n " must not auto-commit :extraction/extract")))))

(deftest consignment-ship-never-auto-at-any-phase
  (testing "structural invariant: no phase, now or in the future entries, auto-commits a real consignment shipment"
    (doseq [[n {:keys [auto]}] phase/phases]
      (is (not (contains? auto :consignment/ship))
          (str "phase " n " must not auto-commit :consignment/ship")))))

(deftest robotics-simulate-quarry-face-verification-never-auto-at-any-phase
  (testing "the robot bench-face/quarry-face verification mission carries no direct capital risk, but is still never auto-eligible, matching every sibling verification op in this fleet"
    (doseq [[n {:keys [auto]}] phase/phases]
      (is (not (contains? auto :robotics/simulate-quarry-face-verification))
          (str "phase " n " must not auto-commit :robotics/simulate-quarry-face-verification")))))

(deftest robotics-simulate-quarry-face-verification-enabled-from-phase-2
  (is (contains? (:writes (get phase/phases 2)) :robotics/simulate-quarry-face-verification))
  (is (contains? (:writes (get phase/phases 3)) :robotics/simulate-quarry-face-verification))
  (is (not (contains? (:writes (get phase/phases 1)) :robotics/simulate-quarry-face-verification))))

(deftest phase-0-is-fully-read-only
  (is (empty? (:writes (get phase/phases 0)))))

(deftest phase-3-auto-commits-only-no-capital-risk-ops
  (testing ":extraction/intake carries no direct capital risk -- auto-eligible; it is the ONLY auto-eligible op in this domain"
    (is (= #{:extraction/intake} (:auto (get phase/phases 3))))))

(deftest gate-hold-always-wins
  (is (= :hold (:disposition (phase/gate 3 {:op :extraction/intake} :hold)))))

(deftest gate-escalates-a-clean-non-auto-write
  (is (= :escalate (:disposition (phase/gate 3 {:op :extraction/extract} :commit))))
  (is (= :escalate (:disposition (phase/gate 3 {:op :consignment/ship} :commit)))))

(deftest gate-holds-a-write-disabled-in-this-phase
  (is (= :hold (:disposition (phase/gate 0 {:op :extraction/intake} :commit)))))
