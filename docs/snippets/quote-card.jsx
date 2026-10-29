// Quote cards for the wall of love and the introduction page. Styles live in /style.css (`.fk-quote*`).
// Usage:
//   import { QuoteGrid, QuoteCard } from '/snippets/quote-card.jsx'
//   <QuoteGrid layout="wall">
//     <QuoteCard quote="..." name="Jane Doe" role="Maintainer of App" avatar="janedoe" handle="@jane" platform="GitHub" date="Mar 15, 2026" href="https://..." />
//   </QuoteGrid>
// `layout="wall"` is a masonry of any number of cards; `layout="row"` puts up to three cards side by side.
// `avatar` is a GitHub login used for the profile picture; without it the card shows the author's initial.
// Quotes are exact: keep the original wording and mark cuts with "[...]". Line breaks in `quote` are kept.
export const QuoteGrid = ({ layout = 'wall', children }) => (
  <div className={`not-prose fk-quotes fk-quotes-${layout}`}>
    <div>{children}</div>
  </div>
)

export const QuoteCard = ({ quote, name, role, avatar, handle, platform, date, href }) => (
  <figure className="fk-quote">
    <span className="fk-quote-mark" aria-hidden="true">“</span>
    <blockquote>{quote}</blockquote>
    <figcaption>
      <span className="fk-quote-author">
        {avatar ? (
          <img className="fk-quote-avatar" src={`https://github.com/${avatar}.png?size=96`} alt="" loading="lazy" noZoom />
        ) : (
          <span className="fk-quote-avatar" aria-hidden="true">{name.charAt(0)}</span>
        )}
        <span>
          <span className="fk-quote-name">{name}</span>
          {role ? <span className="fk-quote-meta">{role}</span> : null}
        </span>
      </span>
      <a className="fk-quote-meta fk-quote-source" href={href} target="_blank" rel="noopener noreferrer">
        {handle ? `${handle} on ` : ''}{platform}, {date}
      </a>
    </figcaption>
  </figure>
)
