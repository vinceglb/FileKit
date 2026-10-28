// Platform availability row shown at the top of feature pages.
// Usage:
//   import { Platforms } from '/snippets/platforms.jsx'
//   <Platforms scope="dialogs" supported="android ios macos jvm windows" partial={{ linux: 'Needs the XDG Desktop Portal', web: 'No directory or dialog settings' }} />
// Platforms of the scope that are neither supported nor partial render as unavailable.
// When only one platform is available, the row shows just that platform ("Web only").
export const Platforms = ({ scope = 'core', supported = '', partial = {}, anchor }) => {
  const labels = {
    android: 'Android',
    ios: 'iOS',
    macos: 'macOS',
    jvm: 'JVM',
    linux: 'Linux',
    windows: 'Windows',
    watchos: 'watchOS',
    web: 'Web',
  }
  const columns = {
    core: ['android', 'ios', 'macos', 'jvm', 'linux', 'windows', 'watchos', 'web'],
    dialogs: ['android', 'ios', 'macos', 'jvm', 'linux', 'windows', 'web'],
    compose: ['android', 'ios', 'macos', 'jvm', 'web'],
  }[scope] || []
  const yes = supported.split(/\s+/).filter(Boolean)
  const stateOf = (key) => (yes.includes(key) ? 'yes' : partial[key] ? 'partial' : 'no')
  const available = columns.filter((key) => stateOf(key) !== 'no')
  // A single available platform reads better as "Web only" than as a row of struck-through pills
  const shown = available.length === 1 ? available : columns

  const styles = {
    yes: { background: 'rgba(16, 185, 129, 0.14)', color: 'rgb(5, 150, 105)', border: '1px solid rgba(16, 185, 129, 0.35)' },
    partial: { background: 'rgba(245, 158, 11, 0.12)', color: 'rgb(217, 119, 6)', border: '1px dashed rgba(245, 158, 11, 0.6)' },
    no: { background: 'transparent', color: 'rgba(120, 120, 130, 0.75)', border: '1px solid rgba(120, 120, 130, 0.3)', textDecoration: 'line-through' },
  }
  const icons = { yes: '✓', partial: '!', no: '✕' }
  const titles = { yes: 'Supported', no: 'Not available' }

  return (
    <a
      href={`/platform-support${anchor ? `#${anchor}` : ''}`}
      title="See the platform support matrix"
      style={{ display: 'flex', flexWrap: 'wrap', gap: '6px', margin: '4px 0 20px', textDecoration: 'none', borderBottom: 'none' }}
    >
      {shown.map((key) => {
        const state = stateOf(key)
        const description = state === 'partial' ? `Partial: ${partial[key]}` : titles[state]
        return (
          <span
            key={key}
            title={description}
            aria-label={`${labels[key]}: ${description}`}
            style={{
              ...styles[state],
              display: 'inline-flex',
              alignItems: 'center',
              gap: '4px',
              padding: '2px 10px',
              borderRadius: '9999px',
              fontSize: '12px',
              fontWeight: 500,
              lineHeight: '20px',
              whiteSpace: 'nowrap',
            }}
          >
            <span aria-hidden="true">{icons[state]}</span>
            {labels[key]}
            {shown.length === 1 ? ' only' : ''}
          </span>
        )
      })}
    </a>
  )
}
