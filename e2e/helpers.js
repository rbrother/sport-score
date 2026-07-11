// @ts-check

/**
 * Fails the test if the page has horizontal scroll - this app is used
 * mostly on phones in portrait orientation, so any content wider than the
 * viewport is treated as a layout bug.
 * @param {import('@playwright/test').Page} page
 */
async function expectNoHorizontalOverflow(page) {
  const { scrollWidth, clientWidth } = await page.evaluate(() => ({
    scrollWidth: document.documentElement.scrollWidth,
    clientWidth: document.documentElement.clientWidth,
  }));
  if (scrollWidth > clientWidth) {
    throw new Error(
      `Horizontal overflow detected: scrollWidth=${scrollWidth} > clientWidth=${clientWidth}`);
  }
}

module.exports = { expectNoHorizontalOverflow };
