(ns adamkl.pedestal-async-error-handler.core-test
  (:require [clojure.test :as t]
            [io.pedestal.interceptor.chain :as chain]
            [clojure.core.async :refer [chan put! thread go <!!]]
            [adamkl.pedestal-async-error-handler.core :as async-handler]))

(def test-ex (ex-info "Test error" {}))
(defn error->chan [_]
  (let [c (chan)]
    (put! c test-ex)
    c))

(t/deftest channeled-exceptions
  (t/testing "should wrap interceptor map function"
    (let [int-map {:enter error->chan}
          wrapped-ctx (async-handler/channeled-exception->context int-map)
          wrapped-fn (:enter wrapped-ctx)]
      (t/is (= test-ex
               (::chain/error (go (<!! (wrapped-fn {})))))))

    #_#_(def int-fn #(identity %))
      (channeled-exception->context int-fn)))