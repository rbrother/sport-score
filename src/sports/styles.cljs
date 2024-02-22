(ns sports.styles
  (:require [spade.core :refer [defglobal]]))

(defglobal
  screen
  [:body {:background :black, :color :white, :font-family "sans-serif"}]
  [:div.pad {:padding "6px"}]
  [:.relative {:position "relative"}]
  [:.left {:text-align "left"}]
  [:.bold {:font-weight "bold"}]
  [:.gray {:color "#AAA"}]
  [:.large {:font-size "24px"}]
  [:.winner {:color "#0F0", :font-weight "bold"}]
  [:.loser {:color "#F88"}]
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
  [:div.row-line {:grid-column "1 / -1", :height "1px", :background "#888"}]
  )