(ns sports.schema-test
  (:require [cljs.test :refer-macros [deftest is testing]]
            [malli.core :as m]
            [sports.schema :as schema]))

(def valid-db
  {:years {:2024 {:2024-05-20 [[{:name "Roope" :score 11} {:name "Kari" :score 5}]]}}
   :navigation {:page :year :year :2024}
   :status nil})

(deftest app-db-schema-test
  (testing "accepts a well-formed app-db"
    (is (true? (m/validate schema/AppDb valid-db))))

  (testing "accepts a set where the loser's score is omitted (0 points)"
    (is (true? (m/validate schema/AppDb
                            (assoc-in valid-db [:years :2024 :2024-05-20]
                                      [[{:name "Roope" :score 11} {:name "Kari"}]])))))

  (testing "accepts the initial app-db (no sessions yet, no status)"
    (is (true? (m/validate schema/AppDb {:years {:2023 {} :2024 {}}
                                          :navigation {:page :years}}))))

  (testing "rejects an unknown page keyword"
    (is (false? (m/validate schema/AppDb
                             (assoc-in valid-db [:navigation :page] :not-a-page)))))

  (testing "rejects a set with a non-numeric score"
    (is (false? (m/validate schema/AppDb
                             (assoc-in valid-db [:years :2024 :2024-05-20]
                                       [[{:name "Roope" :score "11"} {:name "Kari" :score 5}]])))))

  (testing "rejects an unexpected top-level key"
    (is (false? (m/validate schema/AppDb (assoc valid-db :bogus true))))))

(deftest validate!-test
  (testing "clears validation-error for a valid db, returns true"
    (is (true? (schema/validate! valid-db [:some-event])))
    (is (nil? @schema/validation-error)))

  (testing "sets a descriptive validation-error for an invalid db, returns false"
    (is (false? (schema/validate! (assoc valid-db :bogus true) [:some-event])))
    (is (some? @schema/validation-error))
    (is (re-find #"Invalid app-db after event" @schema/validation-error))
    ;; restore to valid state so it doesn't leak into other tests
    (schema/validate! valid-db [:some-event])))

(deftest check-schema-interceptor-test
  (testing "keeps a valid new :db effect untouched"
    (let [context {:coeffects {:event [:some-event] :db valid-db}
                    :effects {:db (assoc-in valid-db [:navigation :page] :years)}}
          result ((:after schema/check-schema) context)]
      (is (= :years (get-in result [:effects :db :navigation :page])))))

  (testing "discards an invalid new :db effect, reverting to the previous db"
    (let [context {:coeffects {:event [:some-event] :db valid-db}
                    :effects {:db (assoc valid-db :bogus true)}}
          result ((:after schema/check-schema) context)]
      (is (= valid-db (get-in result [:effects :db])))
      (is (some? @schema/validation-error))
      (schema/validate! valid-db [:some-event]))))
