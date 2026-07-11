// @ts-check
const { defineConfig, devices } = require('@playwright/test');

/**
 * Regression tests that click through the app's real screens (Years -> Year
 * -> Session -> Add Set) and check that the key widgets render and that
 * pages don't overflow horizontally - this app is used mostly on phones in
 * portrait orientation, so horizontal scrolling is treated as a bug.
 *
 * The app loads its data asynchronously from AWS on startup, so tests
 * should wait for real rows to appear rather than relying on fixed
 * timeouts or specific hard-coded data (years/dates/scores), since the
 * underlying dataset changes over time.
 */
module.exports = defineConfig({
  testDir: './e2e',
  timeout: 30_000,
  expect: { timeout: 15_000 },
  fullyParallel: false,
  retries: 0,
  reporter: [['list']],
  use: {
    baseURL: 'http://localhost:8280',
    trace: 'retain-on-failure',
  },
  projects: [
    {
      name: 'mobile',
      use: { ...devices['iPhone 13'], browserName: 'chromium' },
    },
  ],
  webServer: {
    command: 'npx shadow-cljs watch app',
    url: 'http://localhost:8280',
    reuseExistingServer: true,
    timeout: 120_000,
  },
});
