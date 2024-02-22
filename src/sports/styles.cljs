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
  [:.bold {:font-weight "bold"}]
  [:.large {:font-size "24px"}]
  [:.link {:color "#77F", :font-weight "bold", :cursor "pointer"}]
  [:.link:hover {:color "#99F"}]
  [:button.navigation {:border-width 0, :background "#2070FF", :color :black,
                       :padding "8px 20px 8px 20px", :margin "12px"
                       :font-size "16px", :font-weight "bold"
                       :border-radius "6px"
                       :outline 0
                       :box-shadow "0px 12px 12px rgba(1, 1, 1, 0.5)"}]
  [:button.navigation:hover {:background-color "#40A0FF"}]
  [:input {:margin "6px", :padding "4px"}]
  [:select {:margin "6px", :padding "4px"}]
  [:input.score {:width "40px", :text-align "center"}]
  [:input:read-only {:color "#777"}]
  [:div.grid {:display "grid" :border "1px solid gray" :grid-row-gap "8px" :grid-column-gap "16px"
              :margin "8px 0 8px 0" :padding "8px"}]
  )