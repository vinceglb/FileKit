@file:Suppress("DEPRECATION", "ktlint:standard:function-naming", "TestFunctionName")

package io.github.vinceglb.filekit.dialogs

import java.awt.Window
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNull

class FileKitDialogParentTest {
    @Test
    fun FileKitDialogParent_windows_rejectsZeroHandle() {
        assertFailsWith<IllegalArgumentException> {
            FileKitDialogParent.windows(0)
        }
    }

    @Test
    fun FileKitDialogParent_windows_preservesNegativePointerBitPatterns() {
        assertEquals(
            expected = -1L,
            actual = FileKitDialogParent.windows(-1).resolveWindowsHandle { error("Unexpected AWT conversion") },
        )
    }

    @Test
    fun FileKitDialogParent_x11_formatsLowercasePortalIdentifier() {
        assertEquals(
            expected = "x11:1a2b",
            actual = FileKitDialogParent.x11(0x1a2b).resolveXdgPortalParent { error("Unexpected AWT conversion") },
        )
    }

    @Test
    fun FileKitDialogParent_x11_rejectsInvalidXids() {
        listOf(0L, 0x1_0000_0000L).forEach { xid ->
            assertFailsWith<IllegalArgumentException> {
                FileKitDialogParent.x11(xid)
            }
        }
    }

    @Test
    fun FileKitDialogParent_wayland_preservesOpaquePortalToken() {
        assertEquals(
            expected = "wayland:xdg-foreign-token_42",
            actual = FileKitDialogParent.wayland("xdg-foreign-token_42").resolveXdgPortalParent {
                error("Unexpected AWT conversion")
            },
        )
    }

    @Test
    fun FileKitDialogParent_wayland_rejectsUnsafeTokens() {
        listOf("", "token with spaces", "wayland:already-prefixed", "12345", "0xdeadbeef").forEach { token ->
            assertFailsWith<IllegalArgumentException> {
                FileKitDialogParent.wayland(token)
            }
        }
    }

    @Test
    fun FileKitDialogParent_resolvers_rejectPlatformMismatches() {
        assertFailsWith<IllegalArgumentException> {
            FileKitDialogParent.windows(42).resolveXdgPortalParent { error("Unexpected AWT conversion") }
        }
        assertFailsWith<IllegalArgumentException> {
            FileKitDialogParent.x11(42).resolveWindowsHandle { error("Unexpected AWT conversion") }
        }
        assertFailsWith<IllegalArgumentException> {
            FileKitDialogParent.wayland("portal-token").requireAwtWindowOrNull("The Linux fallback")
        }
    }

    @Test
    fun FileKitDialogParent_nullParentRemainsUnparentedForEveryAdapter() {
        val parent: FileKitDialogParent? = null

        assertNull(parent.resolveWindowsHandle { error("Unexpected AWT conversion") })
        assertEquals("", parent.resolveXdgPortalParent { error("Unexpected AWT conversion") })
        assertNull(parent.requireAwtWindowOrNull("The Linux fallback"))
    }

    @Test
    fun FileKitDialogSettings_legacyAdaptersNormalizeIntoCanonicalParent() {
        val legacy = FileKitDialogSettings(title = "Choose", parentWindow = null)
        val native = FileKitDialogSettings(parent = FileKitDialogParent.windows(42))
        val legacyCopy = native.copy(parentWindow = null)

        assertNull(legacy.parent)
        assertNull(legacy.parentWindow)
        assertNull(native.parentWindow)
        assertNull(legacyCopy.parent)
    }

    @Test
    fun FileKitDialogSettings_parentAndParentWindowCannotBeSuppliedTogether() {
        val members = FileKitDialogSettings::class.java.declaredConstructors.asList() +
            FileKitDialogSettings::class.java.declaredMethods.filter { it.name == "copy" }

        assertFalse(
            members.any { member ->
                Window::class.java in member.parameterTypes &&
                    FileKitDialogParent::class.java in member.parameterTypes
            },
        )
    }
}
