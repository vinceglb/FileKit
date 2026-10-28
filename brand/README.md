# FileKit brand

The FileKit visuals are code: HTML/CSS, one WebGL shader and a small SVG generator. Edit a source,
re-render, commit the sources and the exports together.

## Re-render

```sh
cd brand
npm ci
npx playwright install chromium   # first time only
npm run render                    # every asset (a few seconds)
npm run render -- hero            # only assets whose path contains "hero"
npm run preview                   # serve the sources to tweak them live in a browser
```

`npm run preview` prints two URLs: the brand board (every export and the palette on one page) and the live
hero (`src/hero.html`). The hero fills the browser window, so resizing the window previews other formats.
Add `?seed=3.6` to the hero URL to try another background composition without editing any file.

Rendering runs locally only; CI does not build these assets.

## Outputs

| Asset | Path | Size |
| --- | --- | --- |
| README hero | `brand/exports/readme-hero.webp` | 1920×960, rounded corners |
| GitHub social preview | `brand/exports/social-preview.jpg` | 1280×640, upload in the repo settings |
| Docs hero | `docs/brand/hero.webp` | 1920×1080, rounded corners |
| Open Graph image | `docs/brand/og.jpg` | 1200×630 |
| Logo for light backgrounds | `docs/brand/logo-light.svg` | mark + wordmark, outlined |
| Logo for dark backgrounds | `docs/brand/logo-dark.svg` | mark + wordmark, outlined |
| Favicon | `docs/brand/favicon.svg` | 32×32 mark |
| Mark | `brand/exports/mark.svg` | 512×512 mark, e.g. for an avatar |

Assets served by the docs site live in `docs/brand/`, because Mintlify only serves files inside `docs/`.

The heroes are lossy on purpose: the film grain makes lossless PNGs about 3.5 MB each.
They are WebP when the rounded corners need transparency, and JPEG for social cards (GitHub caps the
social preview at 1 MB).

## Sources

| File | What it controls |
| --- | --- |
| `palette.json` | Every color: docs colors, mark gradient, background stops, grain, text blend. Start here. |
| `src/hero.html` | Hero layout: title, subtitle, construction lines. Sizes are in `cqw` (percent of the width). |
| `src/background.js` | The silk gradient shader. |
| `src/mark.js` | The mark: a file sheet with a folded corner, slanted at Inter Bold Italic's 9.4° italic angle. |
| `render.mjs` | Export list (paths, sizes, formats), logo lockup and wordmark outlining. |
| `fonts/` | Inter 4.1 (SIL Open Font License, see `fonts/OFL.txt`). |

### Palette

| Token | Hex | Use |
| --- | --- | --- |
| `docs.primary` | `#1A7F4B` | Docs primary color (white text on it: 5.0:1) |
| `docs.light` | `#E3C266` | Docs accent in dark mode |
| `docs.dark` | `#1A7F4B` | Docs buttons and hover states |
| `ink` | `#07160E` | Wordmark on light backgrounds |
| `background.dark.stops` | `#07160E` `#0F4A2F` `#2E8A55` `#D8B24C` `#FFE6A6` | Hero gradient, darkest to brightest |

To change the hero colors, edit `background.dark.stops`. The shader maps its noise onto these five colors
in order, so keep them sorted from darkest to brightest.

### Hero recipe

- Title: Inter Bold Italic at 6.77% of the image width (130 px on a 1920 px image),
  drawn twice with the `overlay` blend mode so it takes on the colors underneath.
- Subtitle: Inter Regular at 2.6% of the width (50 px on 1920 px), drawn once in `overlay`.
- Construction lines run along the edges of two equal-height cells. Each text is centered on its cap height,
  and the italic title is shifted so its ink, rather than its advance box, sits in the middle.
- Background: domain-warped noise mapped on the five stops, satin highlights, then film grain (`grain`).
  `seed` picks the composition. A negative `focus` brightens the center slightly so the blended text stays readable.

A light variant exists (`palette.json` → `background.light`, `src/hero.html?theme=light`). It uses
`multiply` instead of `overlay`, because white in overlay renders as solid white on a light background.
It is not exported for now.
