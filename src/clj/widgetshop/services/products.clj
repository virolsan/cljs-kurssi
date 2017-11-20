(ns widgetshop.services.products
  (:require [widgetshop.components.http :refer [publish! transit-response]]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET POST]]
            [clojure.java.jdbc :as jdbc]
            [cognitect.transit :as transit]))

(defn fetch-products-for-category [db category]
  (into []
        (comp
          (map #(update % :price double))
          (map #(update % :rating double)))
        (jdbc/query db [(str "SELECT p.id, p.name, p.description, p.price,"
                             "       avg(pr.rating) AS rating, count(pr.rating) AS ratings_count"
                             "  FROM product p"
                             "  JOIN product_category pc ON pc.product_id = p.id "
                             "  LEFT JOIN product_rating pr ON pr.product_id = p.id"
                             " WHERE pc.category_id = ?"
                             " GROUP BY p.id, p.name, p.description, p.price")
                        category])))

(defn fetch-product-categories [db]
  (jdbc/query db ["SELECT c.id, c.name, c.description FROM category c"]))

(defn fetch-ratings-for-product [db product-id]
  (into []
        (comp
          (map #(update % :rating double)))
        (jdbc/query db [(str "SELECT pr.rating"
                             " FROM product_rating pr"
                             " WHERE pr.product_id = ?")
                        product-id])))

(defn save-rating-for-product! [db product-id rating]
  (jdbc/with-db-transaction [db db]
    (jdbc/insert!
      db
      "product_rating"
      {:product_id product-id
       :rating rating})
    (fetch-ratings-for-product db product-id)))

  (defrecord ProductsService []
    component/Lifecycle
    (start [{:keys [db http] :as this}]
      (assoc this ::routes
                  (publish! http
                            (routes
                              (GET "/categories" []
                                (transit-response
                                  (fetch-product-categories db)))
                              (GET "/products/:category" [category]
                                (transit-response
                                  (fetch-products-for-category db (Long/parseLong category))))
                              (GET "/ratings/:product" [product]
                                (transit-response
                                  (fetch-ratings-for-product db (Long/parseLong product))))
                              (POST "/ratings/" {body :body}
                                (let [{id :id
                                       {:keys [rating]} :my-review
                                       :as params}
                                      (-> body
                                          (transit/reader :json)
                                          (transit/read))]
                                  (transit-response
                                    (save-rating-for-product! db id rating))))))))

  (stop [{stop ::routes :as this}]
    (stop)
    (dissoc this ::routes)))
