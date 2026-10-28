// Renders every FileKit brand asset from the sources in this directory.
//
//   npm run render            # all assets
//   npm run render -- hero    # only assets whose path contains "hero"
//   npm run preview           # serve the sources to tweak them live in a browser
//
// Raster images are screenshots of src/hero.html taken by headless Chromium (Playwright).
// SVG logos are generated from src/mark.js and the outlines of Inter Bold Italic (opentype.js).

import { createServer } from 'node:http';
import { mkdir, readFile, writeFile } from 'node:fs/promises';
import { dirname, extname, join, resolve } from 'node:path';
import { fileURLToPath } from 'node:url';
import opentype from 'opentype.js';
import { chromium } from 'playwright';
import { MARK_BOX, markShapes, markSvg } from './src/mark.js';

const BRAND = dirname(fileURLToPath(import.meta.url));
const REPO = resolve(BRAND, '..');
const palette = JSON.parse(await readFile(join(BRAND, 'palette.json'), 'utf8'));

// The film grain makes lossless PNGs heavy (~3.5 MB at 1920 px), so heroes are lossy:
// WebP when the rounded corners need transparency, JPEG for social cards (GitHub caps those at 1 MB).
// Only the dark theme is exported for now; src/hero.html also supports `theme=light`.
const HEROES = [
  { file: 'brand/exports/readme-hero.webp', width: 1920, height: 960, theme: 'dark', rounded: true },
  { file: 'brand/exports/social-preview.jpg', width: 1280, height: 640, theme: 'dark' },
  { file: 'docs/brand/hero.webp', width: 1920, height: 1080, theme: 'dark', rounded: true },
  { file: 'docs/brand/og.jpg', width: 1200, height: 630, theme: 'dark' },
];
const QUALITY = 0.92;

// ---------------------------------------------------------------------------------------------
// SVG logos

const fontBuffer = await readFile(join(BRAND, 'fonts/Inter-BoldItalic.ttf'));
const font = opentype.parse(fontBuffer.buffer.slice(fontBuffer.byteOffset, fontBuffer.byteOffset + fontBuffer.byteLength));

/** "FileKit" as an SVG path whose baseline is at y and cap height equals capHeight. */
function wordmark(x, y, capHeight, fill) {
  const size = (capHeight * font.unitsPerEm) / font.tables.os2.sCapHeight;
  const scale = size / font.unitsPerEm;
  const tracking = -0.02 * size; // same letter-spacing as the hero title
  const glyphs = [...'FileKit'].map((c) => font.charToGlyph(c)); // no shaping needed for plain Latin
  const path = new opentype.Path();
  let pen = x;
  glyphs.forEach((glyph, i) => {
    path.extend(glyph.getPath(pen, y, size));
    const kern = i < glyphs.length - 1 ? font.getKerningValue(glyph, glyphs[i + 1]) : 0;
    pen += (glyph.advanceWidth + kern) * scale + tracking;
  });
  const box = path.getBoundingBox();
  return { svg: `<path d="${path.toPathData(2)}" fill="${fill}"/>`, right: box.x2 };
}

/** Horizontal lockup: mark + wordmark, cropped to the mark's height (24 units). */
function lockup(theme) {
  const markColors = theme === 'dark' ? palette.markOnDark : palette.mark;
  const textColor = theme === 'dark' ? '#FFFFFF' : palette.ink;
  const cap = 17; // wordmark cap height, vertically centered on the sheet
  const left = MARK_BOX.x;
  const text = wordmark(left + MARK_BOX.width + 7, 16 + cap / 2, cap, textColor);
  const width = Math.ceil(text.right - left + 0.5);
  const { y, height } = MARK_BOX;
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${width * 4}" height="${height * 4}" viewBox="${left.toFixed(2)} ${y} ${width} ${height}" fill="none">` +
    `${markShapes(markColors, `fk-${theme}`)}${text.svg}</svg>\n`;
}

const SVGS = [
  { file: 'docs/brand/logo-light.svg', content: () => lockup('light') },
  { file: 'docs/brand/logo-dark.svg', content: () => lockup('dark') },
  { file: 'docs/brand/favicon.svg', content: () => markSvg(palette.mark, 32) + '\n' },
  { file: 'brand/exports/mark.svg', content: () => markSvg(palette.mark, 512) + '\n' },
];

// ---------------------------------------------------------------------------------------------

/** Static server for the HTML sources; serves brand/ and docs/brand/ only. */
async function serve(port = 0) {
  const types = {
    '.html': 'text/html', '.js': 'text/javascript', '.json': 'application/json', '.woff2': 'font/woff2',
    '.ttf': 'font/ttf', '.svg': 'image/svg+xml', '.webp': 'image/webp', '.jpg': 'image/jpeg',
  };
  const roots = [BRAND, join(REPO, 'docs/brand')];
  const server = createServer(async (req, res) => {
    try {
      const path = join(REPO, decodeURIComponent(new URL(req.url, 'http://x').pathname));
      if (!roots.some((root) => path.startsWith(root))) throw new Error('not served');
      res.writeHead(200, { 'content-type': types[extname(path)] ?? 'application/octet-stream', 'cache-control': 'no-store' });
      res.end(await readFile(path));
    } catch {
      res.writeHead(404).end();
    }
  }).listen(port, '127.0.0.1');
  await new Promise((r) => server.once('listening', r));
  return { server, base: `http://127.0.0.1:${server.address().port}` };
}

if (process.argv[2] === '--preview') {
  const { base } = await serve(4173);
  console.log(`Brand board:  ${base}/brand/src/board.html`);
  console.log(`Live hero:    ${base}/brand/src/hero.html  (resize the window to try formats, add ?seed=3.6 to try a composition)`);
  console.log('Ctrl+C to stop.');
} else {
  await render(process.argv[2]);
}

async function render(filter) {
  const wanted = (file) => !filter || file.includes(filter);

  for (const svg of SVGS.filter((s) => wanted(s.file))) {
    await mkdir(dirname(join(REPO, svg.file)), { recursive: true });
    await writeFile(join(REPO, svg.file), svg.content());
    console.log('wrote', svg.file);
  }

  const heroes = HEROES.filter((h) => wanted(h.file));
  if (heroes.length === 0) return;
  const { server, base } = await serve();
  // SwiftShader keeps WebGL output identical across machines with or without a GPU.
  const browser = await chromium.launch({ args: ['--use-angle=swiftshader', '--enable-unsafe-swiftshader'] });
  for (const hero of heroes) {
    const page = await browser.newPage({ viewport: { width: hero.width, height: hero.height } });
    page.on('pageerror', (e) => console.error(hero.file, e.message));
    await page.goto(`${base}/brand/src/hero.html?theme=${hero.theme}&rounded=${hero.rounded ? 1 : 0}`);
    await page.waitForSelector('body[data-ready]');
    await mkdir(dirname(join(REPO, hero.file)), { recursive: true });
    if (hero.file.endsWith('.jpg')) {
      await page.screenshot({ path: join(REPO, hero.file), type: 'jpeg', quality: QUALITY * 100 });
    } else {
      // Chromium encodes WebP (with alpha) itself, so no image tool is needed.
      const png = await page.screenshot({ omitBackground: true });
      const webp = await page.evaluate(async ([data, quality]) => {
        const bitmap = await createImageBitmap(await (await fetch(`data:image/png;base64,${data}`)).blob());
        const canvas = new OffscreenCanvas(bitmap.width, bitmap.height);
        canvas.getContext('2d').drawImage(bitmap, 0, 0);
        const blob = await canvas.convertToBlob({ type: 'image/webp', quality });
        const url = await new Promise((done) => {
          const reader = new FileReader();
          reader.onload = () => done(reader.result);
          reader.readAsDataURL(blob);
        });
        return url.slice(url.indexOf(',') + 1);
      }, [png.toString('base64'), QUALITY]);
      await writeFile(join(REPO, hero.file), Buffer.from(webp, 'base64'));
    }
    await page.close();
    console.log('wrote', hero.file);
  }
  await browser.close();
  server.close();
}
