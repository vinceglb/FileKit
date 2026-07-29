package io.github.vinceglb.filekit.dialogs.platform.mac.foundation

import io.github.vinceglb.filekit.utils.Platform
import io.github.vinceglb.filekit.utils.PlatformUtil
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@Suppress("ktlint:standard:function-naming", "FunctionName")
class FoundationRunnableIntegrationTest {
    @Test
    fun Foundation_executeOnMainThread_whenIdeaRunnableAlreadyExists_executesRunnable() {
        if (PlatformUtil.current != Platform.MacOS) return

        registerForeignIdeaRunnableIfNeeded()
        assertFalse(Foundation.isNil(Foundation.getObjcClass(LEGACY_CLASS_NAME)))
        val executed = AtomicBoolean(false)

        Foundation.executeOnMainThread(
            withAutoreleasePool = true,
            waitUntilDone = true,
            runnable = Runnable { executed.set(true) },
        )

        assertTrue(executed.get())
        assertFalse(Foundation.isNil(Foundation.getObjcClass("FileKitMainThreadRunnable")))
    }

    private fun registerForeignIdeaRunnableIfNeeded() {
        if (!Foundation.isNil(Foundation.getObjcClass(LEGACY_CLASS_NAME))) return

        val nsObject = Foundation.getObjcClass("NSObject")
        check(!Foundation.isNil(nsObject)) {
            "Unable to resolve NSObject while preparing the runnable adapter collision"
        }

        val foreignClass = Foundation.allocateObjcClassPair(nsObject, LEGACY_CLASS_NAME)
        check(!Foundation.isNil(foreignClass)) {
            "Unable to allocate the foreign $LEGACY_CLASS_NAME class"
        }

        Foundation.registerObjcClassPair(foreignClass)
    }

    private companion object {
        const val LEGACY_CLASS_NAME = "IdeaRunnable"
    }
}
