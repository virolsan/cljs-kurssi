(ns widgetshop.main
  "Main entrypoint for the widgetshop frontend."
  (:require [reagent.core :as r]
            [cljsjs.material-ui]
            [cljs-react-material-ui.core :refer [get-mui-theme color]]
            [cljs-react-material-ui.reagent :as ui]
            [cljs-react-material-ui.icons :as ic]
            [widgetshop.app.state :as state :refer [app]]
            [widgetshop.app.products :as products]
            [widgetshop.app.components :as components]))



;; Task 1: refactor this, the listing of products in a category should
;; be its own component (perhaps in another namespace).
;;
;; Task 2: Add actions to add item to cart. See that cart badge is automatically updated.
;;

(defn listaus [e! jutut]
  [:ul
   (for [juttu jutut]
     [:li [:a {:on-click #(e! (->ValitseJuttu juttu))} juttu]])])

(defn widgetshop [app]
  [ui/mui-theme-provider
   {:mui-theme (get-mui-theme
                {:palette {:text-color (color :green600)}})}
   [:div
    [ui/app-bar {:title "Widgetshop!"
                 :icon-element-right
                 (r/as-element [ui/badge {:badge-content (count (:cart app))
                                          :badge-style {:top 12 :right 12}}
                                [ui/icon-button {:tooltip "Checkout"}
                                 (ic/action-shopping-cart)]])}]
    [ui/paper

     ;; Product category selection
     (when-not (= :loading (:categories app))
       [ui/select-field {:floating-label-text "Select product category"
                         :value               (:id (:category app))
                         :on-change           (fn [evt idx value]
                                                ((products/select-category-by-id! value)
                                                  (state/update-state! components/select-product nil)))}
        (for [{:keys [id name] :as category} (:categories app)]
          ^{:key id}
          [ui/menu-item {:value id :primary-text name}])])

     ;; Product listing for the selected category
     (components/products-table app)

     (components/product-view app)

     [ui/raised-button {:label        "Click me"
                        :icon         (ic/social-group)
                        :on-click     #(println "clicked")}]]]])

(defn main-component []
  [widgetshop @app])

(defn ^:export main []
  (products/load-product-categories!)
  (r/render-component [main-component] (.getElementById js/document "app")))

(defn ^:export reload-hook []
  (r/force-update-all))
