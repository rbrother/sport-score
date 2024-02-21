(ns sports.styles
  (:require-macros [garden.def :refer [defcssfn]])
  (:require [spade.core :refer [defglobal]]
            [garden.units :as units]
            [garden.stylesheet :refer [at-media]]))

(defglobal
  screen
  [:body {:background :black, :color :white, :font-family "sans-serif"}]
  [:div.pad {:padding "6px"}]
  [:.relative {:position "relative"}]
  [:.left {:text-align "left"}]
  [:.left {:font-weight "bold"}]
  [:button.navigation {:border-width 0, :background "#2070FF", :color :black,
                       :padding "12px", :margin "12px"
                       :font-size "20px", :font-weight "bold"
                       :border-radius "6px", :min-width "220px"
                       :outline 0
                       :box-shadow "0px 12px 12px rgba(1, 1, 1, 0.5)"}]
  [:button.navigation:hover {:background-color "#40A0FF"}]
  [:input:read-only {:color "#777"}]
  [:div.grid {:display "grid" :border "1px solid gray" :grid-gap "8px"
              :margin "8px 0 8px 0" :padding "8px"}]
  )