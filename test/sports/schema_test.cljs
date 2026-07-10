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
  (testing "does not throw for a valid db"
    (is (nil? (schema/validate! valid-db [:some-event]))))

  (testing "throws with a descriptive message for an invalid db"
    (is (thrown-with-msg?
          js/Error #"Invalid app-db after event"
          (schema/validate! (assoc valid-db :bogus true) [:some-event])))))
