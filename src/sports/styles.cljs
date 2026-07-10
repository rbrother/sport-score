(ns sports.styles
  (:require [spade.core :refer [defglobal]]))

;; ---------------------------------------------------------------------------
;; Design tokens
;; ---------------------------------------------------------------------------

(def color-bg "#0d0f14")
(def color-surface "#171a21")
(def color-surface-2 "#1f232c")
(def color-border "#2a2f3a")
(def color-text "#eef0f4")
(def color-text-dim "#9aa2b1")
(def color-text-faint "#666d7c")

(def color-primary "#4f8cff")
(def color-primary-dim "#2c4a8a")
(def color-primary-bg "rgba(79,140,255,0.14)")

(def color-win "#33d17a")
(def color-win-bg "rgba(51,209,122,0.14)")
(def color-lose "#ff6b6b")
(def color-lose-bg "rgba(255,107,107,0.12)")

(def radius-sm "8px")
(def radius-md "14px")
(def radius-lg "20px")
(def radius-pill "999px")

(def button-base
  {:border (str "1px solid " color-border)
   :background color-surface-2
   :color color-text
   :padding "10px 18px"
   :margin "4px"
   :min-width "44px"
   :min-height "44px"
   :font-size "15px"
   :font-family "inherit"
   :font-weight "600"
   :border-radius radius-pill
   :outline 0
   :cursor "pointer"
   :transition "background .15s ease, border-color .15s ease, transform .08s ease"
   :-webkit-tap-highlight-color "transparent"})

(defglobal
  screen

  ;; ---- resets & base ------------------------------------------------------
  [:html {:-webkit-text-size-adjust "100%" :box-sizing "border-box"}]
  [:body :div :button :input :select :textarea {:box-sizing "border-box"}]
  [:body
   {:background color-bg :color color-text
    :margin 0
    :font-family "-apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Helvetica, Arial, sans-serif"
    :font-size "15px"
    :line-height "1.45"
    :-webkit-font-smoothing "antialiased"}]
  [:#app {:min-height "100vh"}]
  [:h1 :h2 :h3 {:margin "0 0 8px 0"}]
  [:a {:color color-primary}]

  ;; ---- app shell -----------------------------------------------------------
  [:.app-shell {:max-width "560px" :margin "0 auto" :min-height "100vh"
                :background color-bg :padding-bottom "48px"
                :position "relative"}]

  [:.app-bar {:position "sticky" :top 0 :z-index 20
              :display "flex" :align-items "center" :gap "10px"
              :padding "14px 14px"
              :background "rgba(13,15,20,0.92)"
              :-webkit-backdrop-filter "blur(10px)"
              :backdrop-filter "blur(10px)"
              :border-bottom (str "1px solid " color-border)}]
  [:.app-bar-title {:font-size "19px" :font-weight "700" :flex "1 1 auto"
                     :overflow "hidden" :text-overflow "ellipsis" :white-space "nowrap"}]
  [:.app-bar-title :.sub {:color color-text-dim :font-weight "500" :font-size "14px" :display "block"}]
  [:button.back-btn
   {:background "transparent" :border "none" :color color-primary
    :font-size "22px" :font-weight "700" :padding "6px 8px 6px 2px"
    :min-width "36px" :min-height "36px" :border-radius radius-pill
    :cursor "pointer" :line-height 1 :outline 0
    :-webkit-tap-highlight-color "transparent"}]
  [:button.back-btn:active {:background color-surface-2}]
  [:.app-bar-action {:flex "0 0 auto"}]

  [:div.page {:padding "14px 14px 8px 14px"}]

  ;; ---- typography helpers ---------------------------------------------------
  [:div.pad {:padding "6px"}]
  [:.relative {:position "relative"}]
  [:.left {:text-align "left"}]
  [:.bold {:font-weight "700"}]
  [:.gray {:color color-text-dim}]
  [:.large {:font-size "20px"}]
  [:.center {:text-align "center"}]

  [:.winner {:color color-win :font-weight "700"}]
  [:.loser {:color color-lose}]
  [:span.winner, :span.loser
   {:display "inline-block" :padding "2px 8px" :border-radius radius-pill :font-size "13px"}]
  [:span.winner {:background color-win-bg}]
  [:span.loser {:background color-lose-bg}]
  [:div.winner {:font-weight "700"}]
  ;; plain (non-badge) color-coded text, matching surrounding font weight/size
  [:span.text-win {:color color-win :font-weight "400"}]
  [:span.text-lose {:color color-lose :font-weight "400"}]

  [:.link {:color color-primary :font-weight "600" :cursor "pointer"}]
  [:.link:hover {:color "#7dabff"}]

  ;; ---- cards ------------------------------------------------------------
  [:div.card {:background color-surface :border (str "1px solid " color-border)
              :border-radius radius-md
              :margin "0 0 14px 0" :padding "14px"
              :box-shadow "0 1px 2px rgba(0,0,0,0.3)"}]
  [:div.card-title {:font-size "12px" :text-transform "uppercase" :letter-spacing "0.06em"
                     :color color-text-dim :font-weight "700" :margin-bottom "10px"}]

  ;; ---- list rows (years / sessions quick links) --------------------------
  [:div.list-row {:display "flex" :align-items "center" :justify-content "space-between"
                   :padding "12px 4px" :cursor "pointer" :border-radius radius-sm
                   :-webkit-tap-highlight-color "transparent"}]
  [:div.list-row:active {:background color-surface-2}]
  [:div.list-row + :div.list-row {:border-top (str "1px solid " color-border)}]
  [:div.list-row-chevron {:color color-text-faint :font-size "18px" :margin-left "8px"}]

  ;; ---- buttons ------------------------------------------------------------
  [:button.navigation (assoc button-base
                         :background color-primary :border-color color-primary
                         :color "#06101f")]
  [:button.navigation:hover {:background "#699cff"}]
  [:button.navigation:active {:transform "scale(0.97)"}]
  [:button.idle (assoc button-base :background color-surface-2 :color color-text-dim)]
  [:button.idle:hover {:background "#262b36" :color color-text}]
  [:button.idle:active {:transform "scale(0.97)"}]

  [:button.primary-block {:display "block" :width "100%" :text-align "center"
                           :margin "6px 0" :padding "14px 16px" :font-size "16px"}]

  [:button.win-selected (assoc button-base
                           :background color-win :border-color color-win :color "#06210f")]
  [:button.win-idle (assoc button-base :background color-surface-2 :color color-text-dim)]

  ;; ---- form controls --------------------------------------------------------
  [:input {:margin "6px 0" :padding "10px 12px" :font-size "16px"
           :background color-surface-2 :color color-text
           :border (str "1px solid " color-border) :border-radius radius-sm
           :outline 0}]
  [:input:focus {:border-color color-primary}]
  [:select {:margin "6px 0" :padding "10px 12px" :font-size "16px"
            :background color-surface-2 :color color-text
            :border (str "1px solid " color-border) :border-radius radius-sm}]
  [:input.score {:width "48px" :text-align "center"}]
  [:input:read-only {:color color-text-faint}]
  [:textarea {:background color-surface-2 :color color-text
              :border (str "1px solid " color-border) :border-radius radius-sm
              :padding "10px" :font-family "monospace" :font-size "13px" :width "100%"}]
  [:label.field-label {:display "block" :color color-text-dim :font-size "13px"
                        :margin "4px 0 2px 2px"}]

  [:hr {:border "none" :border-top (str "1px solid " color-border) :margin "12px 0"}]

  ;; ---- data tables (CSS grid) --------------------------------------------
  [:div.border {:border (str "1px solid " color-border)
                :border-radius radius-md
                :margin "0 0 12px 0" :padding "12px"}]
  [:div.grid {:display "grid" :grid-row-gap "10px" :grid-column-gap "8px"
              :align-items "center"
              :font-size "14px"
              :margin "0" :padding "2px"}]
  [:.col-header {:font-size "11px" :text-transform "uppercase" :letter-spacing "0.05em"
                 :color color-text-dim :font-weight "700"}]
  [:div.row-line {:grid-column "1 / -1" :height "1px" :background color-border
                  :margin "2px 0"}]
  [:div.table-scroll {:overflow-x "auto" :-webkit-overflow-scrolling "touch"}]

  ;; ---- score chips (sets table) -------------------------------------------
  [:span.score-chip {:font-variant-numeric "tabular-nums" :font-weight "700"}]

  ;; ---- misc ---------------------------------------------------------------
  [:div.empty-state {:color color-text-dim :text-align "center" :padding "24px 10px"}]
  [:div.validation-error-banner {:position "sticky" :top 0 :z-index 30}]

  [:div.chart-card {:background color-surface :border (str "1px solid " color-border)
                     :border-radius radius-md :padding "10px 6px 4px 6px"
                     :margin "4px 0 14px 0"}]

  [:.button-row {:display "flex" :flex-wrap "wrap" :gap "0"}]
  )
