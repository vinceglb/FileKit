// FileKit mark: a file sheet with a folded corner, slanted like Inter Bold Italic.
// Pure functions returning SVG markup, shared by the browser templates and the Node export script.

/** Italic angle of Inter Bold Italic (post.italicAngle), so the sheet leans like the "F". */
export const ITALIC_ANGLE = 9.4;
const SLANT = Math.tan((ITALIC_ANGLE * Math.PI) / 180);

const W = 18; // sheet width
const H = 24; // sheet height
const FOLD = 6; // folded corner size
const R = 2.5; // corner radius

const SHEET = `M${R} 0H${W - FOLD}L${W} ${FOLD}V${H - R}A${R} ${R} 0 0 1 ${W - R} ${H}H${R}A${R} ${R} 0 0 1 0 ${H - R}V${R}A${R} ${R} 0 0 1 ${R} 0Z`;
const FOLD_PATH = `M${W - FOLD} 0V${FOLD - 2}A2 2 0 0 0 ${W - FOLD + 2} ${FOLD}H${W}Z`;

/** Bounding box of the slanted sheet inside the 32x32 mark box, to crop lockups tightly. */
export const MARK_BOX = { x: (32 - (W + SLANT * H)) / 2, y: (32 - H) / 2, width: W + SLANT * H, height: H };

/**
 * Returns the mark's inner SVG (gradient + shapes) in a 32x32 box.
 * @param {string[]} colors three gradient stops, bottom-left to top-right: deep, mid, highlight
 * @param {string} id unique prefix for the gradient id
 */
export function markShapes([deep, mid, highlight], id = 'fk') {
  const x = MARK_BOX.x + SLANT * H; // skewX moves the bottom edge left, so start the top edge further right
  return `<defs><linearGradient id="${id}-g" x1="0" y1="1" x2="1" y2="0">` +
    `<stop stop-color="${deep}"/><stop offset=".55" stop-color="${mid}"/><stop offset="1" stop-color="${highlight}"/>` +
    '</linearGradient></defs>' +
    `<g transform="translate(${x.toFixed(2)} ${MARK_BOX.y}) skewX(${-ITALIC_ANGLE})">` +
    `<path d="${SHEET}" fill="url(#${id}-g)"/>` +
    `<path d="${FOLD_PATH}" fill="#fff" fill-opacity=".55"/>` +
    '</g>';
}

/** Standalone mark SVG document. */
export function markSvg(colors, size = 32, id = 'fk') {
  return `<svg xmlns="http://www.w3.org/2000/svg" width="${size}" height="${size}" viewBox="0 0 32 32">${markShapes(colors, id)}</svg>`;
}
