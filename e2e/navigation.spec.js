// @ts-check
const { test, expect } = require('@playwright/test');
const { expectNoHorizontalOverflow } = require('./helpers');

/**
 * End-to-end regression test that walks through the app's real screens in
 * the same order a user would: Years -> Year -> Session, then back again.
 * It checks that the key widgets on each screen render with the *exact*
 * expected values, and that no screen requires horizontal scrolling on a
 * phone-sized viewport (see playwright.config.js - tests run with an
 * iPhone 13 device profile).
 *
 * This test is pinned to year 2025 and session 2025-01-07, which are fully
 * in the past - the app only ever adds new sessions/sets going forward, so
 * this historical data is frozen and safe to assert on exactly. (Tests
 * that don't depend on specific data - e.g. reaching the Add Set/Add
 * Session forms - instead just follow whatever the most recent year/session
 * happens to be; see below.)
 */

test('walk through Years -> Year 2025 -> Session 2025-01-07', async ({ page }) => {
  // ---- Years page ---------------------------------------------------------
  await page.goto('/');
  await expect(page.locator('.app-bar-title')).toContainText('Sport Tracker');
  await expect(page.locator('div.col-header', { hasText: 'Year' })).toBeVisible();
  await expect(page.locator('div.col-header', { hasText: 'Sessions' })).toBeVisible();

  const yearLink = page.locator('div.card div.link', { hasText: '2025' });
  await expect(yearLink).toBeVisible();
  await expectNoHorizontalOverflow(page);

  // ---- Year page: 2025 -------------------------------------------------------
  await yearLink.click();
  await expect(page).toHaveURL(/\/year\/2025$/);
  await expect(page.locator('.app-bar-title')).toContainText('Year :2025');

  await expect(page.locator('.card-title', { hasText: 'Sessions' })).toBeVisible();
  await expect(page.locator('canvas')).toBeVisible(); // score development chart

  // 2025 is complete history: 37 sessions, fixed year totals (rendered twice:
  // a TOTAL row above and below the per-session rows).
  const yearGrid = page.locator('div.grid').first();
  await expect(yearGrid.getByText('37')).toBeVisible();
  await expect(yearGrid.getByText('TOTAL').first()).toBeVisible();
  await expect(yearGrid.getByText('53.0').first()).toBeVisible();
  await expect(yearGrid.getByText('17.5').first()).toBeVisible();
  await expect(yearGrid.getByText('45.5').first()).toBeVisible();

  await expectNoHorizontalOverflow(page);

  const sessionLink = page.locator('div.card div.link', { hasText: '2025-01-07' });
  await expect(sessionLink).toBeVisible();

  // ---- Session page: 2025-01-07 ----------------------------------------------
  await sessionLink.click();
  await expect(page).toHaveURL(/\/session\/2025-01-07$/);
  await expect(page.locator('.app-bar-title')).toContainText('Session :2025-01-07');

  // Totals card: fixed points for this session.
  const totalsCard = page.locator('.card', { has: page.locator('.card-title', { hasText: 'Totals' }) });
  await expect(totalsCard).toContainText('Roope');
  await expect(totalsCard).toContainText('2.5');
  await expect(totalsCard).toContainText('Kari');
  await expect(totalsCard).toContainText('1.5');
  await expect(totalsCard).toContainText('Niklas');

  // Head-to-head card: fixed pair results.
  const h2hCard = page.locator('.card', { has: page.locator('.card-title', { hasText: 'Head-to-head' }) });
  await expect(h2hCard).toContainText('Kari - Roope');
  await expect(h2hCard).toContainText('Niklas - Roope');
  await expect(h2hCard).toContainText('Niklas - Kari');

  // Sets card: 10 sets played, most recent one is Roope beating Niklas 11-7.
  const setsCard = page.locator('.card', { has: page.locator('.card-title', { hasText: 'Sets' }) });
  await expect(setsCard.locator('div.grid > div').nth(4)).toHaveText('10'); // first data row's "#"
  await expect(setsCard).toContainText('11');
  await expect(setsCard).toContainText('Niklas');

  await expectNoHorizontalOverflow(page);

  // ---- Back navigation -------------------------------------------------------
  await page.locator('button.back-btn').click();
  await expect(page).toHaveURL(/\/year\/2025$/);
});

/**
 * These tests just check that the "add" forms are reachable and render
 * their key widgets - they don't depend on specific data, so they follow
 * whatever the most recent (first) year/session happens to be.
 */
test('Add Set screen is reachable from a session page', async ({ page }) => {
  await page.goto('/');
  const yearLink = page.locator('div.card div.link').first();
  await expect(yearLink).toBeVisible();
  await yearLink.click();

  const sessionLink = page.locator('div.card div.link').first();
  await expect(sessionLink).toBeVisible();
  await sessionLink.click();

  await page.getByRole('button', { name: '+ Set' }).click();
  await expect(page).toHaveURL(/\/add-set$/);
  await expect(page.locator('.app-bar-title')).toContainText('New Set');
  await expect(page.getByText('🏆 Winner')).toBeVisible();
  await expect(page.getByText('Loser', { exact: true })).toBeVisible();
  await expect(page.getByRole('button', { name: 'Roope' }).first()).toBeVisible();

  await expectNoHorizontalOverflow(page);
});

test('Add Session screen is reachable from the Year page', async ({ page }) => {
  await page.goto('/');
  const yearLink = page.locator('div.card div.link').first();
  await expect(yearLink).toBeVisible();
  await yearLink.click();

  await page.getByRole('button', { name: '+ Session' }).click();
  await expect(page).toHaveURL(/\/add-session$/);
  await expect(page.locator('.app-bar-title')).toContainText('Add Session');
  await expect(page.getByText('Date (YYYY-MM-DD)')).toBeVisible();
  await expect(page.getByRole('button', { name: 'Create Session' })).toBeVisible();

  await expectNoHorizontalOverflow(page);
});
