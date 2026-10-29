// Text-only "used by" banner for the introduction page (no logos). Styles live in /style.css (`.fk-used-by*`).
// Usage:
//   import { UsedBy } from '/snippets/used-by.jsx'
//   <UsedBy href="/showcase" items={[{ name: 'Thunderbird', detail: 'Email for Android' }]} />
export const UsedBy = ({ items = [], href = '/showcase' }) => (
  <div className="not-prose fk-used-by">
    <div>
      {items.map((item) => (
        <a key={item.name} href={href}>
          <span className="fk-used-by-name">{item.name}</span>
          <span className="fk-used-by-detail">{item.detail}</span>
        </a>
      ))}
    </div>
  </div>
)
