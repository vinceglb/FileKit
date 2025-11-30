package io.github.vinceglb.filekit.dialogs.platform.windows.util

import com.sun.jna.platform.win32.Guid

/**
 * Using Guid.CLSID or Guid.IID when using proguard and obfuscate crashes the application.
 *
 * This is due to annotation @FieldOrder not being applied to the fields in the class.
 *
 * This is a workaround to use the fixed CLSID and IID.
 * Declaring getFieldOrder() in the class works as expected.
 */
internal object GuidFixed {
    class CLSID(
        guid: String,
    ) : Guid.CLSID(guid) {
        override fun getFieldOrder(): List<String> =
            listOf("Data1", "Data2", "Data3", "Data4")
    }

    class IID(
        iid: String,
    ) : Guid.IID(iid) {
        override fun getFieldOrder(): List<String> =
            listOf("Data1", "Data2", "Data3", "Data4")
    }
}
