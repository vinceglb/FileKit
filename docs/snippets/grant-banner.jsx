/**
 * Grant banner for the docs: the brand strip, then a short line linking to the announcement.
 * The images follow the docs light/dark theme (rendered by brand/render.mjs).
 *
 * Usage:
 *   import { GrantBanner, grantPostUrl } from "/snippets/grant-banner.jsx"
 *
 *   <GrantBanner />
 */
export const grantPostUrl = "https://kotlinfoundation.org/news/grants-program-winners-26/"

export const GrantBanner = () => {
  const alt = "FileKit is supported by the Kotlin Foundation Grants Program"
  return (
    <a
      href={grantPostUrl}
      className="not-prose group my-6 block overflow-hidden rounded-2xl border border-gray-950/10 dark:border-white/10"
    >
      <img src="/brand/grant-banner-light.webp" alt={alt} width="1920" height="240" className="block w-full dark:hidden" />
      <img src="/brand/grant-banner-dark.webp" alt={alt} width="1920" height="240" className="hidden w-full dark:block" />
      <p className="px-5 py-3 text-sm text-gray-700 dark:text-gray-300">
        The grant supports the work toward FileKit 1.0.{" "}
        <span className="whitespace-nowrap font-semibold text-primary group-hover:underline dark:text-primary-light">
          Read the announcement →
        </span>
      </p>
    </a>
  )
}
