// @ts-check
const { test, expect } = require('@playwright/test');
const { expectNoHorizontalOverflow } = require('./helpers');

test('marks missing payer details after the first recorded payment', async ({ page }) => {
  const sets = [[{ name: 'Roope', score: 11 }, { name: 'Kari', score: 5 }]];
  let years = {
    '2026': {
      '2026-01-03': sets,
      '2026-01-01': sets,
      '2026-01-02': { sets, payment: { 'cost-cents': 0, payer: 'Niklas' } },
    },
    '2027': { '2027-01-01': sets },
  };
  await page.route('**/prod/**', async route => {
    const request = route.request();
    const headers = {
      'access-control-allow-origin': '*',
      'access-control-allow-headers': '*',
      'access-control-allow-methods': 'POST, OPTIONS',
    };
    if (request.method() === 'OPTIONS') {
      await route.fulfill({ status: 204, headers });
    } else if (request.url().endsWith('/download')) {
      await route.fulfill({ json: { body: years }, headers });
    } else if (request.url().endsWith('/upload')) {
      years = request.postDataJSON().content;
      await route.fulfill({ json: {}, headers });
    } else {
      await route.abort();
    }
  });

  await page.goto('/');
  await page.locator('div.card div.link', { hasText: '2026' }).click();
  const warning = page.getByRole('img', { name: 'Payer information missing' });
  await expect(warning).toHaveCount(1);
  const grid = page.locator('.card', { hasText: 'Sessions' }).locator('.grid');
  await expect(grid.locator('div.link', { hasText: '2026-01-03' })
    .locator('xpath=following-sibling::div[5]').getByRole('img', { name: 'Payer information missing' })).toBeVisible();
  await expectNoHorizontalOverflow(page);
  await grid.locator('div.link', { hasText: '2026-01-03' }).click();
  await page.getByLabel('Total cost (€)').fill('0');
  await page.getByLabel('Paid by').selectOption('Roope');
  await page.getByRole('button', { name: 'Save rent' }).click();
  await expect(page.getByRole('status')).toContainText('Data Upload Success');
  await page.locator('button.back-btn').click();
  await expect(warning).toHaveCount(0);
  await page.locator('button.back-btn').click();
  await page.locator('div.card div.link', { hasText: '2027' }).click();
  await expect(warning).toHaveCount(1);
});

test('shows separate rent balances on year pages, not the home page', async ({ page }) => {
  await page.route('**/prod/**', async route => {
    const headers = {
      'access-control-allow-origin': '*',
      'access-control-allow-headers': '*',
      'access-control-allow-methods': 'POST, OPTIONS',
    };
    if (route.request().method() === 'OPTIONS') {
      await route.fulfill({ status: 204, headers });
    } else if (route.request().url().endsWith('/download')) {
      await route.fulfill({
        headers,
        json: { body: { '2027': {}, '2026': { '2026-12-31': {
          sets: [[{ name: 'Roope' }, { name: 'Kari' }]],
          payment: { 'cost-cents': 1200, payer: 'Niklas' },
        } } } },
      });
    } else {
      await route.abort();
    }
  });
  await page.goto('/');
  await expect(page.locator('.card-title', { hasText: 'Rent balance' })).toHaveCount(0);
  await page.locator('div.card div.link', { hasText: '2026' }).click();
  await expect(page.locator('.card', { hasText: 'Rent balance 2026' })).toContainText('€12');
  await page.locator('button.back-btn').click();
  await page.locator('div.card div.link', { hasText: '2027' }).click();
  await expect(page.locator('.card', { hasText: 'Rent balance 2027' }).getByText('€0', { exact: true })).toHaveCount(3);
});

test('records rent, reloads it and recalculates shares when another player joins', async ({ page }) => {
  let years = {
    '2026': {
      '2026-02-01': [[{ name: 'Roope', score: 11 }, { name: 'Kari', score: 5 }]],
    },
  };
  let uploads = 0;
  await page.route('**/prod/**', async route => {
    const request = route.request();
    const headers = {
      'access-control-allow-origin': '*',
      'access-control-allow-headers': '*',
      'access-control-allow-methods': 'POST, OPTIONS',
    };
    if (request.method() === 'OPTIONS') {
      await route.fulfill({ status: 204, headers });
    } else if (request.url().endsWith('/download')) {
      await route.fulfill({ json: { body: years }, headers });
    } else if (request.url().endsWith('/upload')) {
      years = request.postDataJSON().content;
      uploads += 1;
      await route.fulfill({ json: {}, headers });
    } else {
      await route.abort();
    }
  });

  await page.goto('/');
  await page.locator('div.card div.link', { hasText: '2026' }).click();
  await page.locator('div.card div.link', { hasText: '2026-02-01' }).click();
  await expect(page.locator('.page > .card > .card-title')).toHaveText([
    'Totals', 'Head-to-head', 'Sets', 'Hall rent',
  ]);
  await expect(page.getByText('Paid by (Suggestion: Roope)')).toBeVisible();
  await page.getByLabel('Total cost (€)').fill('12.50');
  await page.getByLabel('Paid by').selectOption('Niklas');
  await page.getByRole('button', { name: 'Save rent' }).click();
  await expect(page.getByText('Cost: €13 · Paid by Niklas')).toBeVisible();
  await expect.poll(() => uploads).toBe(1);
  expect(years['2026']['2026-02-01'].payment['cost-cents']).toBe(1250);
  await expectNoHorizontalOverflow(page);

  await page.reload();
  await expect(page.getByLabel('Total cost (€)')).toHaveValue('12.50');
  await expect(page.getByLabel('Paid by')).toHaveValue('Niklas');
  await expect(page.getByText('Paid by (Suggestion: Roope)')).toBeVisible();
  await page.locator('button.back-btn').click();
  const balances = page.locator('.card', { hasText: 'Rent balance 2026' });
  await expect(balances).toContainText('-€6');
  await expect(balances).toContainText('€13');
  await page.locator('div.card div.link', { hasText: '2026-02-01' }).click();
  await page.getByRole('button', { name: '+ Set' }).click();
  await page.getByRole('button', { name: 'Niklas' }).first().click();
  await page.getByRole('button', { name: 'Kari' }).last().click();
  await page.getByRole('button', { name: 'Add Set ✓' }).click();
  await expect(page.getByText('Cost: €13 · Paid by Niklas')).toBeVisible();
  await expect(page.getByText('Sharing: Roope, Kari, Niklas')).toBeVisible();
  await expect.poll(() => uploads).toBe(2);
  expect(years['2026']['2026-02-01'].payment['cost-cents']).toBe(1250);
  await page.locator('button.back-btn').click();
  await expect(balances).toContainText('-€4');
  await expect(balances).toContainText('€8');
  await expectNoHorizontalOverflow(page);

  await page.locator('div.card div.link', { hasText: '2026-02-01' }).click();
  await page.getByLabel('Total cost (€)').fill('');
  await page.getByRole('button', { name: 'Save rent' }).click();
  await expect.poll(() => uploads).toBe(3);
  await page.reload();
  await expect(page.getByText('No cost recorded (counts as €0).')).toBeVisible();
  await page.locator('button.back-btn').click();
  await expect(balances.getByText('€0', { exact: true })).toHaveCount(3);
});
