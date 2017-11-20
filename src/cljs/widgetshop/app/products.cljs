(ns widgetshop.app.products
  "Controls product listing information."
  (:require [widgetshop.app.state :refer [update-state! set-state!]]
            [widgetshop.server :as server]
            [cognitect.transit :as transit]))

(defn select-category-by-id! [category-id]
  (update-state!
   (fn [{:keys [categories] :as app}]
     (let [category (some #(when (= (:id %) category-id) %) categories)]
       (server/get! (str "/products/" (:id category))
                    {:on-success #(set-state! [:products-by-category category] %)})
       (-> app
           (assoc :category category)
           (assoc-in [:products-by-category category] :loading))))))

(defn load-product-categories! []
  (server/get! "/categories" {:on-success #(set-state! [:categories] %)}))

(defn add-rating! [product rating]
  (println (str "Adding rating " rating " for product " product))
  (server/post! "/ratings/"
                {:params {:id (:id product) :my-review rating}
                 :on-success #(println "Success")
                 :on-failure #(println "Failure")}))