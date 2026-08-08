package io.github.vinceglb.filekit.dialogs.platform.mac.foundation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertSame

@Suppress("ktlint:standard:function-naming", "FunctionName")
class ObjcRunnableClassRegistrationTest {
    @Test
    fun ObjcRunnableClassRegistration_nullAllocation_rejectsBeforeNativeMutation() {
        val events = mutableListOf<String>()

        val exception = assertFailsWith<FoundationRunnableBootstrapException> {
            registerObjcRunnableClass(
                className = CLASS_NAME,
                allocate = {
                    events += "allocate $it"
                    null
                },
                addMethod = {
                    events += "add method"
                    true
                },
                register = { events += "register" },
                dispose = { events += "dispose" },
            )
        }

        assertEquals("Unable to allocate Objective-C runnable adapter class '$CLASS_NAME'", exception.message)
        assertEquals(listOf("allocate $CLASS_NAME"), events)
    }

    @Test
    fun ObjcRunnableClassRegistration_zeroValuedAllocation_rejectsBeforeNativeMutation() {
        val events = mutableListOf<String>()

        val exception = assertFailsWith<FoundationRunnableBootstrapException> {
            registerObjcRunnableClass(
                className = CLASS_NAME,
                allocate = {
                    events += "allocate $it"
                    ID(0)
                },
                addMethod = {
                    events += "add method"
                    true
                },
                register = { events += "register" },
                dispose = { events += "dispose" },
            )
        }

        assertEquals("Unable to allocate Objective-C runnable adapter class '$CLASS_NAME'", exception.message)
        assertEquals(listOf("allocate $CLASS_NAME"), events)
    }

    @Test
    fun ObjcRunnableClassRegistration_addMethodFailure_disposesBeforeRegistration() {
        val runnableClass = ID(42)
        val events = mutableListOf<String>()

        val exception = assertFailsWith<FoundationRunnableBootstrapException> {
            registerObjcRunnableClass(
                className = CLASS_NAME,
                allocate = {
                    events += "allocate $it"
                    runnableClass
                },
                addMethod = {
                    assertSame(runnableClass, it)
                    events += "add method"
                    false
                },
                register = { events += "register" },
                dispose = {
                    assertSame(runnableClass, it)
                    events += "dispose"
                },
            )
        }

        assertEquals("Unable to add run: method to Objective-C runnable adapter class '$CLASS_NAME'", exception.message)
        assertEquals(listOf("allocate $CLASS_NAME", "add method", "dispose"), events)
    }

    @Test
    fun ObjcRunnableClassRegistration_success_addsMethodBeforeRegistration() {
        val runnableClass = ID(42)
        val events = mutableListOf<String>()

        val result = registerObjcRunnableClass(
            className = CLASS_NAME,
            allocate = {
                events += "allocate $it"
                runnableClass
            },
            addMethod = {
                assertSame(runnableClass, it)
                events += "add method"
                true
            },
            register = {
                assertSame(runnableClass, it)
                events += "register"
            },
            dispose = { events += "dispose" },
        )

        assertSame(runnableClass, result)
        assertEquals(listOf("allocate $CLASS_NAME", "add method", "register"), events)
    }

    private companion object {
        const val CLASS_NAME = "FileKitMainThreadRunnable"
    }
}
