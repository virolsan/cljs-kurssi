(ns widgetshop.app.components
  "Contains widgetshop UI components."
  (:require [widgetshop.app.state :as state :refer [update-state! set-state!]]
            [widgetshop.app.products :as products]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as ic]))

(defn- add-to-cart [app product]
  (println "add-to-cart" product)
  (update app :cart conj product))

(defn select-product [app product]
  (println "select-product" product)
  (assoc app :selected-product product))

(defn update-rating [app rating]
  (println "update-rating" rating)
  (assoc app :rating rating))

(defn add-rating [app product rating]
  (products/add-rating! product rating)
  (state/update-state! update-rating {})) ;; TODO average rating on product

(defn average-rating [product]
  (let [ratings (:ratings product)
        rating-count (count ratings)
        rating-sum (apply + (map :rating ratings))]
    (println "Average of " ratings " = " rating-sum "/" rating-count)
    (/ rating-sum rating-count)))

(defn product-view [{:keys [id name description price] :as product} rating-form]
  (when product
    [ui/card
     [ui/card-title name]
     [ui/card-text description]
     [ui/card-text (str "Price " price "€")]
     [ui/card-text (str "Rating ")
      (for [star (range (average-rating product))]
        ^{:key star} [ic/toggle-star])]

     [ui/slider {:step 1 :min 0 :max 5
                 :on-change (fn [event value]
                              (state/update-state! update-rating (assoc-in rating-form [:rating] value)))}]
     [ui/card-text "Comments: "
      [ui/text-field {:id "comment" :label "Comments"
                      :on-change (fn [event value]
                                   (state/update-state! update-rating (assoc-in rating-form [:comment] value)))}]]
     [ui/card-actions
      [ui/flat-button {:on-click #(state/update-state! add-rating product rating-form)} "Rate!"]
      [ui/flat-button {:on-click #(state/update-state! select-product nil)} "Close"]]]))

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
        (for [{:keys [id name description price rating] :as product} products]
          ^{:key id}
          [ui/table-row
           [ui/table-row-column [:a {:on-click #(state/update-state! select-product product)} name]]
           [ui/table-row-column description]
           [ui/table-row-column price]
           [ui/table-row-column rating]
           [ui/table-row-column
            [ui/flat-button {:primary true :on-click #(state/update-state! add-to-cart product)}
             "Add to cart"]]])]])))

