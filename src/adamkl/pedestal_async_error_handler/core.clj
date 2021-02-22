(ns adamkl.pedestal-async-error-handler.core
  (:require  [io.pedestal.interceptor.chain :as chain]
             [io.pedestal.interceptor :as interceptor]
             [clojure.core.async :refer [go <! chan put!]]))

(defn- channeled-exception->context-wrapper [interceptor key]
  (if-let [old-fn (key interceptor)]
    (assoc interceptor
           key
           (fn [old-context]
             (go
               (let [maybe-new-context (<! (old-fn old-context))]
                 (if (instance? Throwable maybe-new-context)
                   (assoc old-context ::chain/error maybe-new-context)
                   maybe-new-context)))))
    interceptor))

(defn channeled-exception->context [handler]
  (-> handler
      (interceptor/interceptor)
      (channeled-exception->context-wrapper :enter)
      (channeled-exception->context-wrapper :leave)))


(comment
  (defn error->chan [_]
    (let [c (chan)]
      (put! c (ex-info "Test error" {}))
      c))

  (def int-map {:enter error->chan})
  (channeled-exception->context int-map)

  (def int-fn error->chan)
  (channeled-exception->context int-fn)
  (comment))