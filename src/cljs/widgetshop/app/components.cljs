(ns widgetshop.app.components
  "Contains widgetshop UI components."
  (:require [widgetshop.app.state :refer [update-state! set-state!]]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as ic]))

(defn get-product-by-id [id app]
  (if (nil? id)
    nil
    (let [products ((:products-by-category app) (:category app))]
      (first (filter (fn [product] (= id (:id product))) products)))))

(defn add-to-cart! [product app]
  (println "add-to-cart" product)
  (update-state! (fn [] (set-state! [:cart] (conj (:cart app) product)))))

(defn select-product! [product app]
  (println "select-product" product)
  (update-state! (fn [] (set-state! [:product] product))))

(defn update-rating! [product rating app]
  (println "update-rating" rating)
  (select-product! (assoc product :rating rating) app))

(defn product-view [app]
  (let [product (:product app)]
    (if (not (nil? product))
      [ui/card
       [ui/card-title (:name product)]
       [ui/card-text (:description product)]
       [ui/card-text (str "Price " (:price product) "€")]
       [ui/card-text (str "Rating ")
        (for [rating (range (:rating product))] [ic/toggle-star])]
       [ui/slider {:step 1 :min 0 :max 5 :on-change (fn [event value] (update-rating! product value app))}]
       [ui/card-actions
        [ui/flat-button {:on-click #(select-product! nil app)} "Sulje"]]])))

(defn products-table [app]
  (let [products ((:products-by-category app) (:category app))]
    (if (= :loading products)
      [ui/refresh-indicator {:status "loading" :size 40 :left 10 :top 10}]

      [ui/table
       [ui/table-header {:display-select-all false :adjust-for-checkbox false}
        [ui/table-row
         [ui/table-header-column "Name"]
         [ui/table-header-column "Description"]
         [ui/table-header-column "Price (€)"]
         [ui/table-header-column "Rating"]
         [ui/table-header-column "Add to cart"]]]
       [ui/table-body {:display-row-checkbox false}
        (for [{:keys [id name description price rating]} products]
          ^{:key id}
          [ui/table-row
           [ui/table-row-column [:a {:on-click #(select-product! (get-product-by-id id app) app)} name]]
           [ui/table-row-column description]
           [ui/table-row-column price]
           [ui/table-row-column rating]
           [ui/table-row-column
            [ui/flat-button {:primary true :on-click #(add-to-cart! (get-product-by-id id app) app)}
             "Add to cart"]]])]])))

