@file:Suppress("ktlint:standard:function-naming", "FunctionName")

package io.github.vinceglb.filekit.dialogs

import org.junit.Assume.assumeFalse
import java.awt.Frame
import java.awt.GraphicsEnvironment
import java.lang.reflect.Modifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals
import kotlin.test.assertNull
import kotlin.test.assertSame
import kotlin.test.assertTrue

class FileKitDialogParentTest {
    @Test
    fun FileKitDialogParent_windowsWithZero_throwsIllegalArgumentException() {
        assertFailsWith<IllegalArgumentException> {
            FileKitDialogParent.windows(0)
        }
    }

    @Test
    fun FileKitDialogParent_windowsWithNegativeBitPattern_preservesHandle() {
        val parent = FileKitDialogParent.windows(-1)

        assertEquals(-1, parent.resolveWindowsHandle { error("AWT conversion must not run") })
    }

    @Test
    fun FileKitDialogParent_x11WithBoundaryValues_serializesCanonicalPortalParents() {
        assertEquals("x11:1", FileKitDialogParent.x11(1).resolveXdgPortalParent { error("unused") })
        assertEquals("x11:2a", FileKitDialogParent.x11(0x2a).resolveXdgPortalParent { error("unused") })
        assertEquals(
            "x11:ffffffff",
            FileKitDialogParent.x11(0xffff_ffff).resolveXdgPortalParent { error("unused") },
        )
    }

    @Test
    fun FileKitDialogParent_x11OutsideUnsigned32BitRange_throwsIllegalArgumentException() {
        listOf(-1L, 0L, 0x1_0000_0000L).forEach { xid ->
            assertFailsWith<IllegalArgumentException> {
                FileKitDialogParent.x11(xid)
            }
        }
    }

    @Test
    fun FileKitDialogParent_waylandWithOpaqueValue_preservesEveryCharacter() {
        listOf(
            "xdg-foreign-token",
            " token with spaces ",
            "12345",
            "0xdeadbeef",
            "wayland:already-prefixed",
        ).forEach { token ->
            assertEquals(
                "wayland:$token",
                FileKitDialogParent.wayland(token).resolveXdgPortalParent { error("unused") },
            )
        }
    }

    @Test
    fun FileKitDialogParent_waylandWithEmptyOrNul_throwsIllegalArgumentException() {
        listOf("", "before\u0000after").forEach { token ->
            assertFailsWith<IllegalArgumentException> {
                FileKitDialogParent.wayland(token)
            }
        }
    }

    @Test
    fun FileKitDialogParent_withNativeValue_redactsDiagnosticString() {
        assertEquals("FileKitDialogParent.Windows", FileKitDialogParent.windows(0x1234).toString())
        assertEquals("FileKitDialogParent.X11", FileKitDialogParent.x11(0x5678).toString())
        assertEquals("FileKitDialogParent.Wayland", FileKitDialogParent.wayland("secret-token").toString())
    }

    @Test
    fun FileKitDialogParent_concreteVariants_arePrivateInJvmBytecode() {
        val variantNames = setOf("Awt", "Windows", "X11", "Wayland")
        val variants = FileKitDialogParent::class.java.declaredClasses
            .filter { it.simpleName in variantNames }

        assertEquals(variantNames, variants.map { it.simpleName }.toSet())
        variants.forEach { variant ->
            assertTrue(Modifier.isPrivate(variant.modifiers), "${variant.simpleName} must remain private")
        }
    }

    @Test
    fun FileKitDialogParent_withIncompatibleAdapter_throwsInvalidInvocationWithoutRawValue() {
        val error = assertFailsWith<IllegalArgumentException> {
            FileKitDialogParent.windows(0x1234).resolveXdgPortalParent { error("unused") }
        }

        assertNotEquals(true, error.message?.contains("1234"))
    }

    @Test
    fun FileKitDialogParent_withoutParent_resolvesAsUnparented() {
        val parent: FileKitDialogParent? = null

        assertNull(parent.resolveWindowsHandle { error("unused") })
        assertEquals("", parent.resolveXdgPortalParent { error("unused") })
    }

    @Test
    fun AwtNativeIdentifier_withZeroOrException_throwsInvalidInvocation() {
        assertFailsWith<IllegalArgumentException> {
            resolveAwtNativeIdentifier("Windows HWND") { 0 }
        }

        val cause = IllegalStateException("Component must be displayable")
        val error = assertFailsWith<IllegalArgumentException> {
            resolveAwtNativeIdentifier("X11 XID") { throw cause }
        }

        assertSame(cause, error.cause)
    }

    @Test
    fun FileKitDialogParent_awtWithSameWindow_hasStructuralEquality() {
        assumeFalse(GraphicsEnvironment.isHeadless())
        val frame = Frame()

        try {
            assertEquals(FileKitDialogParent.awt(frame), FileKitDialogParent.awt(frame))
            assertEquals("FileKitDialogParent.Awt", FileKitDialogParent.awt(frame).toString())
        } finally {
            frame.dispose()
        }
    }
}
