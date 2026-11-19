package io.github.vinceglb.filekit

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withContext
import org.w3c.files.Blob
import kotlin.js.ExperimentalWasmJsInterop
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

class FileKitDownloadWebTest {
    @BeforeTest
    fun setUp() {
        installObjectUrlProbe()
    }

    @AfterTest
    fun tearDown() {
        uninstallObjectUrlProbe()
    }

    @Test
    fun downloadBlob_afterRevokeDelay_revokesEveryObjectUrl() = runTest {
        repeat(3) { index ->
            downloadBlob(
                blob = Blob(),
                fileName = "hello-$index.txt",
                revokeDelay = 10.milliseconds,
            )
        }
        assertEquals(3, createdObjectUrlCount())

        waitRealTime(200.milliseconds)

        assertEquals(0, activeObjectUrlCount())
    }

    @Test
    fun download_afterClick_keepsObjectUrlAlive() = runTest {
        FileKit.download(
            bytes = "Hello, World!".encodeToByteArray(),
            fileName = "hello.txt",
        )
        assertEquals(1, createdObjectUrlCount())
        assertEquals(1, activeObjectUrlCount())

        // Revoking right after the click can cancel the download in some browsers.
        waitRealTime(200.milliseconds)

        assertEquals(1, activeObjectUrlCount())
    }

    @Test
    fun downloadBlob_clickThrows_revokesObjectUrlAndRethrows() {
        makeAnchorClickThrow()
        try {
            assertFails {
                downloadBlob(blob = Blob(), fileName = "hello.txt")
            }
        } finally {
            restoreAnchorClick()
        }

        assertEquals(1, createdObjectUrlCount())
        assertEquals(0, activeObjectUrlCount())
    }
}

// runTest skips delays in virtual time, so switch to a real dispatcher to let browser timers run.
private suspend fun waitRealTime(duration: Duration) {
    withContext(Dispatchers.Default) { delay(duration) }
}

// Wraps URL.createObjectURL and URL.revokeObjectURL to record every URL created and revoked.
@OptIn(ExperimentalWasmJsInterop::class)
private fun installObjectUrlProbe(): Unit = js(
    """
    (() => {
        const probe = {
            created: [],
            revoked: [],
            originalCreate: URL.createObjectURL,
            originalRevoke: URL.revokeObjectURL,
        };
        URL.createObjectURL = function (obj) {
            const url = probe.originalCreate.call(URL, obj);
            probe.created.push(url);
            return url;
        };
        URL.revokeObjectURL = function (url) {
            probe.revoked.push(url);
            return probe.originalRevoke.call(URL, url);
        };
        globalThis.filekitObjectUrlProbe = probe;
    })()
    """,
)

@OptIn(ExperimentalWasmJsInterop::class)
private fun uninstallObjectUrlProbe(): Unit = js(
    """
    (() => {
        const probe = globalThis.filekitObjectUrlProbe;
        if (probe) {
            URL.createObjectURL = probe.originalCreate;
            URL.revokeObjectURL = probe.originalRevoke;
            delete globalThis.filekitObjectUrlProbe;
        }
    })()
    """,
)

@OptIn(ExperimentalWasmJsInterop::class)
private fun createdObjectUrlCount(): Int = js("globalThis.filekitObjectUrlProbe.created.length")

@OptIn(ExperimentalWasmJsInterop::class)
private fun activeObjectUrlCount(): Int = js(
    """
    globalThis.filekitObjectUrlProbe.created
        .filter((url) => !globalThis.filekitObjectUrlProbe.revoked.includes(url))
        .length
    """,
)

// Shadows the inherited HTMLElement.prototype.click for anchors only.
@OptIn(ExperimentalWasmJsInterop::class)
private fun makeAnchorClickThrow(): Unit = js(
    """
    (() => {
        HTMLAnchorElement.prototype.click = function () {
            throw new Error("click failed");
        };
    })()
    """,
)

@OptIn(ExperimentalWasmJsInterop::class)
private fun restoreAnchorClick(): Unit = js("(() => { delete HTMLAnchorElement.prototype.click; })()")
