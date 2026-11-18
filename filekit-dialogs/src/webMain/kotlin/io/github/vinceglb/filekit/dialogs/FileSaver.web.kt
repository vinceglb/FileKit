@file:OptIn(ExperimentalFileKitApi::class)

package io.github.vinceglb.filekit.dialogs

import io.github.vinceglb.filekit.ExperimentalFileKitApi
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.FileKitCoreInternalApi
import io.github.vinceglb.filekit.PlatformFile
import io.github.vinceglb.filekit.createSaveDestination
import kotlinx.coroutines.await
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.js.JsAny
import kotlin.js.Promise

/**
 * Opens a browser save picker when the File System Access API is available.
 *
 * Browsers without the API, and cross-origin frames where the browser denies access to it, use
 * download behavior when the returned file is written. [directory] and [dialogSettings] are ignored
 * on web. The picker must be called during a user activation, such as directly from a click handler.
 *
 * @param suggestedName The suggested file name without the default extension.
 * @param defaultExtension The default file extension without the dot.
 * @param allowedExtensions The file extensions offered by the save picker, without dots.
 * @param directory Ignored on web.
 * @param dialogSettings Ignored on web.
 * @return The selected save destination, or `null` if the user cancelled.
 * @throws FileKitDialogException If the picker is rejected for a reason other than cancellation.
 */
@OptIn(ExperimentalWasmJsInterop::class, FileKitCoreInternalApi::class)
@Suppress("UNUSED_PARAMETER")
@ExperimentalFileKitApi
public suspend fun FileKit.openFileSaver(
    suggestedName: String,
    defaultExtension: String? = null,
    allowedExtensions: Set<String>? = null,
    directory: PlatformFile? = null,
    dialogSettings: FileKitDialogSettings = FileKitDialogSettings.createDefault(),
): PlatformFile? {
    val fileName = buildFileSaverSuggestedName(suggestedName, defaultExtension)
    val extensions = buildFileSaverAllowedFileTypes(defaultExtension, allowedExtensions)
        ?.joinToString(separator = "\u0000")
        .orEmpty()
    val result = invokeBrowserSavePicker(fileName, extensions, browserSavePickerTestOverride).await()

    return when (result.kind) {
        "unsupported" -> createSaveDestination(handle = null, name = fileName)
        "selected" -> createSaveDestination(handle = result.handle, name = fileName)
        "error" -> {
            if (result.errorName == "AbortError") {
                null
            } else {
                throw FileKitDialogException(
                    "Could not open the web file saver (${result.errorName}): ${result.errorMessage}",
                )
            }
        }

        else -> throw FileKitDialogException("The browser returned an unknown file saver result")
    }
}

@OptIn(ExperimentalWasmJsInterop::class)
private external interface BrowserSavePickerResult : JsAny {
    val kind: String
    val handle: JsAny?
    val errorName: String
    val errorMessage: String
}

/** Optional picker replacement used by shared web tests. */
@OptIn(ExperimentalWasmJsInterop::class)
internal var browserSavePickerTestOverride: JsAny? = null

/**
 * Calls the API before returning its promise so the call remains within the caller's user activation.
 * Feature detection and both synchronous and asynchronous error handling stay in this JS boundary.
 */
@OptIn(ExperimentalWasmJsInterop::class)
private fun invokeBrowserSavePicker(
    suggestedName: String,
    extensionList: String,
    testOverride: JsAny?,
): Promise<BrowserSavePickerResult> = js(
    """
    {
        let crossOriginFrame = false;
        let hasUserActivation = true;
        const handleError = function(error) {
            const errorName = String(error && error.name || "Error");
            const errorMessage = String(error && error.message || error || "Unknown error");
            if (errorName === "SecurityError" && crossOriginFrame && hasUserActivation) {
                return { kind: "unsupported" };
            }
            return { kind: "error", errorName: errorName, errorMessage: errorMessage };
        };

        try {
            const api = testOverride === null
                ? globalThis.showSaveFilePicker
                : (testOverride.available ? testOverride.picker : null);
            if (typeof api !== "function") {
                return Promise.resolve({ kind: "unsupported" });
            }

            try {
                crossOriginFrame = window.self !== window.top &&
                    window.parent.location.origin !== window.location.origin;
            } catch (_) {
                crossOriginFrame = true;
            }

            const userActivation = navigator.userActivation;
            hasUserActivation = userActivation == null || userActivation.isActive;
            const options = { suggestedName: suggestedName };
            const extensions = extensionList ? extensionList.split("\u0000") : [];

            if (extensions.length > 0) {
                const mimeByExtension = Object.assign(Object.create(null), {
                    txt: "text/plain", text: "text/plain", csv: "text/csv", tsv: "text/tab-separated-values",
                    html: "text/html", htm: "text/html", css: "text/css", xml: "application/xml",
                    json: "application/json", pdf: "application/pdf", md: "text/markdown",
                    png: "image/png", jpg: "image/jpeg", jpeg: "image/jpeg", gif: "image/gif",
                    svg: "image/svg+xml", webp: "image/webp", bmp: "image/bmp", ico: "image/x-icon",
                    mp3: "audio/mpeg", wav: "audio/wav", mp4: "video/mp4", webm: "video/webm",
                    zip: "application/zip", gz: "application/gzip", tar: "application/x-tar",
                    doc: "application/msword", docx: "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    xls: "application/vnd.ms-excel", xlsx: "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    ppt: "application/vnd.ms-powerpoint", pptx: "application/vnd.openxmlformats-officedocument.presentationml.presentation"
                });
                const accept = {};
                for (const extension of extensions) {
                    const key = extension.toLowerCase();
                    const mime = mimeByExtension[key] || "application/octet-stream";
                    if (!accept[mime]) accept[mime] = [];
                    accept[mime].push("." + extension);
                }
                options.types = [{ description: "Files", accept: accept }];
            }

            return Promise.resolve(api.call(globalThis, options)).then(
                function(handle) { return { kind: "selected", handle: handle }; },
                handleError
            );
        } catch (error) {
            return Promise.resolve(handleError(error));
        }
    }
    """,
)
