// Quote cards for the wall of love and the introduction page.
// Usage:
//   import { QuoteGrid, QuoteCard } from '/snippets/quote-card.jsx'
//   <QuoteGrid>
//     <QuoteCard quote="..." name="Jane Doe" handle="@jane" platform="GitHub" date="Mar 15, 2026" href="https://..." role="Maintainer of App" />
//   </QuoteGrid>
// Quotes are exact: keep the original wording and mark cuts with "[...]". Line breaks in `quote` are kept.
// Colors are translucent so the cards read well in light and dark mode.
export const QuoteGrid = ({ children }) => (
  <div className="not-prose" style={{ columnWidth: '15rem', columnGap: '1rem', margin: '1.5rem 0' }}>
    {children}
  </div>
)

export const QuoteCard = ({ quote, name, handle, platform, date, href, role }) => (
  <figure
    style={{
      breakInside: 'avoid',
      margin: '0 0 1rem',
      padding: '1.25rem',
      borderRadius: '1rem',
      border: '1px solid rgba(120, 120, 130, 0.25)',
      background: 'rgba(120, 120, 130, 0.06)',
    }}
  >
    <blockquote style={{ margin: 0, padding: 0, border: 'none', fontStyle: 'normal', fontSize: '0.95rem', lineHeight: 1.6, whiteSpace: 'pre-line' }}>
      {quote}
    </blockquote>
    <figcaption style={{ marginTop: '1rem', fontSize: '0.85rem', lineHeight: 1.4 }}>
      <div style={{ fontWeight: 600 }}>{name}</div>
      {role ? <div style={{ opacity: 0.7 }}>{role}</div> : null}
      <a
        href={href}
        target="_blank"
        rel="noopener noreferrer"
        style={{ display: 'inline-block', marginTop: '0.25rem', opacity: 0.7, color: 'inherit', textDecoration: 'underline', textUnderlineOffset: '2px' }}
      >
        {handle} on {platform}, {date}
      </a>
    </figcaption>
  </figure>
)
