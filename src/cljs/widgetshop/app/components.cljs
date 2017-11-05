(ns widgetshop.app.components
  "Contains widgetshop UI components."
  (:require [widgetshop.app.state :as state :refer [update-state! set-state!]]
            [widgetshop.app.products :as products]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as ic]))

(defn add-to-cart [app product]
  (println "add-to-cart" product)
  (update app :cart product))

(defn select-product [app product]
  (println "select-product" product)
  (assoc app :selected-product product))

(defn update-rating [app rating]
  (println "update-rating" rating)
  (update app :rating rating))

(defn add-rating! [product app]
  (products/add-rating! product (:rating app))) ;; TODO average rating on product

(defn product-view [app]
  (let [product (:selected-product app)
        rating-form (:rating app)]
    (if (not (nil? product))
      [ui/card
       [ui/card-title (:name product)]
       [ui/card-text (:description product)]
       [ui/card-text (str "Price " (:price product) "€")]
       [ui/card-text (str "Rating ")
        (for [star (range (:rating product))]
          ^{:key star} [ic/toggle-star])]

       [ui/divider]

       [ui/slider {:step 1 :min 0 :max 5
                   :on-change (fn [event value]
                                (state/update-state! update-rating (assoc-in rating-form [:rating] value)))}]
       [ui/card-text "Comments: "
        [ui/text-field {:id "comment" :label "Comments" :defaultValue (:comment rating-form)
                        :on-change (fn [event value]
                                     (state/update-state! update-rating (assoc-in rating-form [:comment] value)))}]]
       [ui/card-actions
        [ui/flat-button {:on-click #(add-rating! product app)} "Rate!"]
        [ui/flat-button {:on-click #(state/update-state! select-product nil)} "Close"]]])))

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

