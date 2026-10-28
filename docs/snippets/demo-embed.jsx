/**
 * Embeds the FileKit web sample (Compose Multiplatform, WebAssembly) in a docs page.
 *
 * The sample is only downloaded when the reader clicks "Run live demo", so pages stay fast.
 * The embed follows the docs light/dark theme.
 *
 * Usage:
 *   import { FileKitDemo } from "/snippets/demo-embed.jsx"
 *
 *   <FileKitDemo demo="file-picker" title="File picker" />
 *
 * `demo` is one of: file-picker, gallery-picker, directory-picker, camera-picker, file-saver, share-file.
 * Omit it to open the sample home screen.
 */
export const FileKitDemo = ({ demo, title = "FileKit sample", height = 560 }) => {
  const sampleUrl = "https://vinceglb.github.io/FileKit/"
  const sampleOrigin = new URL(sampleUrl).origin
  const [running, setRunning] = useState(false)
  const [src, setSrc] = useState(null)
  const [dark, setDark] = useState(false)
  const iframeRef = useRef(null)

  useEffect(() => {
    const root = document.documentElement
    const update = () => setDark(root.classList.contains("dark"))
    update()
    const observer = new MutationObserver(update)
    observer.observe(root, { attributes: true, attributeFilter: ["class"] })
    return () => observer.disconnect()
  }, [])

  // Theme changes after launch are forwarded to the running sample without reloading it
  const postTheme = () => {
    const frame = iframeRef.current
    if (frame && frame.contentWindow) {
      frame.contentWindow.postMessage(`filekit-demo:theme=${dark ? "dark" : "light"}`, sampleOrigin)
    }
  }

  useEffect(postTheme, [dark])

  // The launch button disappears on click, so keyboard focus moves to the demo
  useEffect(() => {
    if (running && iframeRef.current) iframeRef.current.focus()
  }, [running])

  const params = new URLSearchParams({ embed: "true" })
  if (demo) params.set("demo", demo)
  const standaloneUrl = demo ? `${sampleUrl}?demo=${demo}` : sampleUrl

  const run = () => {
    params.set("theme", dark ? "dark" : "light")
    setSrc(`${sampleUrl}?${params.toString()}`)
    setRunning(true)
  }

  return (
    <div className="not-prose my-6">
      <div
        className="relative w-full overflow-hidden rounded-2xl border border-gray-950/10 dark:border-white/10 bg-gray-50 dark:bg-white/5"
        style={{ height: `${height}px` }}
      >
        {running ? (
          <iframe
            ref={iframeRef}
            src={src}
            title={`${title} live demo`}
            onLoad={postTheme}
            className="w-full h-full border-0"
          />
        ) : (
          <div className="flex h-full w-full flex-col items-center justify-center gap-3 px-6 text-center">
            <p className="text-base font-semibold text-gray-900 dark:text-gray-100">{title}</p>
            <p className="max-w-sm text-sm text-gray-600 dark:text-gray-400">
              Try FileKit in your browser. The demo runs the Compose Multiplatform sample with WebAssembly
              (about 5 MB on first launch). Files you pick never leave your device.
            </p>
            <button
              type="button"
              onClick={run}
              className="mt-2 rounded-full bg-primary px-5 py-2 text-sm font-semibold text-white hover:opacity-90 dark:bg-primary-light dark:text-gray-950"
            >
              <span aria-hidden="true">▶ </span>Run live demo
            </button>
          </div>
        )}
      </div>
      <p className="mt-2 text-right text-xs text-gray-500 dark:text-gray-400">
        <a href={standaloneUrl} target="_blank" rel="noopener noreferrer" className="underline">
          Open in a new tab ↗
        </a>
      </p>
    </div>
  )
}
