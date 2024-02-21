(ns sports.styles
  (:require-macros [garden.def :refer [defcssfn]])
  (:require [spade.core :refer [defglobal]]
            [garden.units :as units]
            [garden.stylesheet :refer [at-media]]))

(defglobal screen
  [:body {:background :black, :color :white, :font-family "sans-serif"}]
  [:div.pad {:padding "6px"}]
  [:table {:border "3px solid #666"}]
  [:tbody {:margin "12px" :border "3px solid #444"}]
  [:th {:padding "4px" :background "white" :color "black" :font-weight "bold"}]
  [:td {:padding "4px" :border-left "1px solid #444" :border-top "1px solid #444"
        :margin 0}]
  [:.money {:font-family "monospace" :text-align "right" :width "60px"}]
  [:tr:hover {:background-color "#222"}]
  [:tr.main {:font-size "18px"}]
  [:tr.sub {:font-size "14px"}]
  [:td.main {:width "200px"}]
  [:td.sub {:padding-left "30px"}]
  [:td:hover {:background-color "#333"}]
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
  [:input:read-only {:color "#777"}])