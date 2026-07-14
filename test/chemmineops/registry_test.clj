(ns chemmineops.registry-test
  (:require [clojure.test :refer [deftest is]]
            [chemmineops.registry :as r]))

;; ----------------------------- royalty-matches-claim? -----------------------------

(deftest matches-when-claim-equals-recompute
  (is (r/royalty-matches-claim?
       {:quantity 100 :royalty-rate 2.0 :claimed-royalty 200.0})))

(deftest mismatches-when-claim-differs-from-recompute
  (is (not (r/royalty-matches-claim?
            {:quantity 20 :royalty-rate 5.0 :claimed-royalty 150.0}))))

(deftest compute-royalty-is-a-flat-quantity-times-rate
  (is (= 200.0 (r/compute-royalty {:quantity 100 :royalty-rate 2.0}))))

;; ----------------------------- register-extraction -----------------------------

(deftest extraction-is-a-draft-not-a-real-extraction
  (let [result (r/register-extraction "extraction-1" "JPN" 0)]
    (is (nil? (get-in result ["certificate" "proof"])))
    (is (= (get-in result ["certificate" "issued_by_registry"]) false))
    (is (= (get-in result ["certificate" "status"]) "draft-unsigned"))))

(deftest extraction-assigns-extraction-number
  (let [result (r/register-extraction "extraction-1" "JPN" 7)]
    (is (= (get result "extraction_number") "JPN-EXT-000007"))
    (is (= (get-in result ["record" "extraction_id"]) "extraction-1"))
    (is (= (get-in result ["record" "kind"]) "extraction-draft"))
    (is (= (get-in result ["record" "immutable"]) true))))

(deftest extraction-validation-rules
  (is (thrown? Exception (r/register-extraction "" "JPN" 0)))
  (is (thrown? Exception (r/register-extraction "extraction-1" "" 0)))
  (is (thrown? Exception (r/register-extraction "extraction-1" "JPN" -1))))

;; ----------------------------- register-consignment-shipment -----------------------------

(deftest shipment-is-a-draft-not-a-real-shipment
  (let [result (r/register-consignment-shipment "extraction-1" "JPN" 0)]
    (is (nil? (get-in result ["certificate" "proof"])))
    (is (= (get-in result ["certificate" "issued_by_registry"]) false))
    (is (= (get-in result ["certificate" "status"]) "draft-unsigned"))))

(deftest shipment-assigns-shipment-number
  (let [result (r/register-consignment-shipment "extraction-1" "JPN" 7)]
    (is (= (get result "shipment_number") "JPN-SHP-000007"))
    (is (= (get-in result ["record" "extraction_id"]) "extraction-1"))
    (is (= (get-in result ["record" "kind"]) "consignment-shipment-draft"))
    (is (= (get-in result ["record" "immutable"]) true))))

(deftest shipment-validation-rules
  (is (thrown? Exception (r/register-consignment-shipment "" "JPN" 0)))
  (is (thrown? Exception (r/register-consignment-shipment "extraction-1" "" 0)))
  (is (thrown? Exception (r/register-consignment-shipment "extraction-1" "JPN" -1))))

(deftest history-is-append-only
  (let [c1 (r/register-extraction "extraction-1" "JPN" 0)
        hist (r/append [] c1)
        c2 (r/register-extraction "extraction-2" "JPN" 1)
        hist2 (r/append hist c2)]
    (is (= 2 (count hist2)))
    (is (= "JPN-EXT-000000" (get-in hist2 [0 "record_id"])))
    (is (= "JPN-EXT-000001" (get-in hist2 [1 "record_id"])))))
