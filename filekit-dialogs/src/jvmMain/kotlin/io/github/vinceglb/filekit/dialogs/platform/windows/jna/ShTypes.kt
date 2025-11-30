package io.github.vinceglb.filekit.dialogs.platform.windows.jna

import com.sun.jna.Structure
import com.sun.jna.Structure.FieldOrder
import com.sun.jna.WString
import com.sun.jna.platform.win32.Guid.GUID

internal interface ShTypes {
    @Suppress("ktlint:standard:class-naming")
    @FieldOrder("pszName", "pszSpec")
    class COMDLG_FILTERSPEC : Structure() {
        @JvmField
        var pszName: WString? = null

        @JvmField
        var pszSpec: WString? = null

        override fun getFieldOrder(): List<String> = listOf("pszName", "pszSpec")
    }

    @FieldOrder("fmdid", "pid")
    class PROPERTYKEY : Structure() {
        @JvmField
        var fmtid: GUID? = null

        @JvmField
        var pid: Int = 0

        override fun getFieldOrder(): List<String> = listOf("fmtid", "pid")
    }

    interface FILEOPENDIALOGOPTIONS {
        companion object {
            const val FOS_OVERWRITEPROMPT: Int = 0x2
            const val FOS_STRICTFILETYPES: Int = 0x4
            const val FOS_NOCHANGEDIR: Int = 0x8
            const val FOS_PICKFOLDERS: Int = 0x20
            const val FOS_FORCEFILESYSTEM: Int = 0x40
            const val FOS_ALLNONSTORAGEITEMS: Int = 0x80
            const val FOS_NOVALIDATE: Int = 0x100
            const val FOS_ALLOWMULTISELECT: Int = 0x200
            const val FOS_PATHMUSTEXIST: Int = 0x800
            const val FOS_FILEMUSTEXIST: Int = 0x1000
            const val FOS_CREATEPROMPT: Int = 0x2000
            const val FOS_SHAREAWARE: Int = 0x4000
            const val FOS_NOREADONLYRETURN: Int = 0x8000
            const val FOS_NOTESTFILECREATE: Int = 0x10000
            const val FOS_HIDEMRUPLACES: Int = 0x20000
            const val FOS_HIDEPINNEDPLACES: Int = 0x40000
            const val FOS_NODEREFERENCELINKS: Int = 0x100000
            const val FOS_OKBUTTONNEEDSINTERACTION: Int = 0x200000
            const val FOS_DONTADDTORECENT: Int = 0x2000000
            const val FOS_FORCESHOWHIDDEN: Int = 0x10000000
            const val FOS_DEFAULTNOMINIMODE: Int = 0x20000000
            const val FOS_FORCEPREVIEWPANEON: Int = 0x40000000
            const val FOS_SUPPORTSTREAMABLEITEMS: Long = 0x80000000
        }
    }

    interface SIATTRIBFLAGS {
        companion object {
            const val SIATTRIBFLAGS_AND: Int = 0x1
            const val SIATTRIBFLAGS_OR: Int = 0x2
            const val SIATTRIBFLAGS_APPCOMPAT: Int = 0x3
            const val SIATTRIBFLAGS_MASK: Int = 0x3
            const val SIATTRIBFLAGS_ALLITEMS: Int = 0x4000
        }
    }

    interface SFGAOF {
        companion object {
            const val SFGAO_CANCOPY: Int = 0x1 // Objects can be copied (DROPEFFECT_COPY)
            const val SFGAO_CANMOVE: Int = 0x2 // Objects can be moved (DROPEFFECT_MOVE)
            const val SFGAO_CANLINK: Int = 0x4 // Objects can be linked (DROPEFFECT_LINK)
            const val SFGAO_STORAGE: Int = 0x00000008 // supports BindToObject(IID_IStorage)
            const val SFGAO_CANRENAME: Int = 0x00000010 // Objects can be renamed
            const val SFGAO_CANDELETE: Int = 0x00000020 // Objects can be deleted
            const val SFGAO_HASPROPSHEET: Int = 0x00000040 // Objects have property sheets
            const val SFGAO_DROPTARGET: Int = 0x00000100 // Objects are drop target
            const val SFGAO_CAPABILITYMASK: Int = 0x00000177
            const val SFGAO_ENCRYPTED: Int =
                0x00002000 // object is encrypted (use alt color)
            const val SFGAO_ISSLOW: Int = 0x00004000 // 'slow' object
            const val SFGAO_GHOSTED: Int = 0x00008000 // ghosted icon
            const val SFGAO_LINK: Int = 0x00010000 // Shortcut (link)
            const val SFGAO_SHARE: Int = 0x00020000 // shared
            const val SFGAO_READONLY: Int = 0x00040000 // read-only
            const val SFGAO_HIDDEN: Int = 0x00080000 // hidden object
            const val SFGAO_DISPLAYATTRMASK: Int = 0x000FC000
            const val SFGAO_FILESYSANCESTOR: Int =
                0x10000000 // may contain children with int SFGAO_FILESYSTEM
            const val SFGAO_FOLDER: Int =
                0x20000000 // support BindToObject(IID_IShellFolder)
            const val SFGAO_FILESYSTEM: Int =
                0x40000000 // is a win32 file system object (file/folder/root)
            const val SFGAO_HASSUBFOLDER: Int =
                -0x80000000 // may contain children with int SFGAO_FOLDER
            const val SFGAO_CONTENTSMASK: Int = -0x80000000
            const val SFGAO_VALIDATE: Int = 0x01000000 // invalidate cached information
            const val SFGAO_REMOVABLE: Int = 0x02000000 // is this removeable media?
            const val SFGAO_COMPRESSED: Int =
                0x04000000 // Object is compressed (use alt color)
            const val SFGAO_BROWSABLE: Int =
                0x08000000 // supports IShellFolder, but only implements CreateViewObject() (non-folder

            // view)
            const val SFGAO_NONENUMERATED: Int = 0x00100000 // is a non-enumerated object
            const val SFGAO_NEWCONTENT: Int = 0x00200000 // should show bold in explorer tree
            const val SFGAO_CANMONIKER: Int = 0x00400000 // defunct
            const val SFGAO_HASSTORAGE: Int = 0x00400000 // defunct
            const val SFGAO_STREAM: Int = 0x00400000 // supports BindToObject(IID_IStream)
            const val SFGAO_STORAGEANCESTOR: Int =
                0x00800000 // may contain children with int SFGAO_STORAGE or int SFGAO_STREAM
            const val SFGAO_STORAGECAPMASK: Int =
                0x70C50008 // for determining storage capabilities, ie for open/save semantics
        }
    }

    interface GETPROPERTYSTOREFLAGS {
        companion object {
            // If no flags are specified (int GPS_DEFAULT), a read-only property store is
            // returned that includes properties for the file or item.
            // In the case that the shell item is a file, the property store contains:
            // 1. properties about the file from the file system
            // 2. properties from the file itself provided by the file's property handler,
            // unless that file is offline, see int GPS_OPENSLOWITEM
            // 3. if requested by the file's property handler and supported by the file
            // system, properties stored in the alternate property store.
            //
            // Non-file shell items should return a similar read-only store
            //
            // Specifying other int GPS_ flags modifies the store that is returned
            const val GPS_DEFAULT: Int = 0x00000000
            const val GPS_HANDLERPROPERTIESONLY: Int =
                0x00000001 // only include properties directly from the file's property handler
            const val GPS_READWRITE: Int =
                0x00000002 // Writable stores will only include handler properties
            const val GPS_TEMPORARY: Int =
                0x00000004 // A read/write store that only holds properties for the lifetime of the

            // IShellItem object
            const val GPS_FASTPROPERTIESONLY: Int =
                0x00000008 // do not include any properties from the file's property handler

            // (because the file's property handler will hit the disk)
            const val GPS_OPENSLOWITEM: Int =
                0x00000010 // include properties from a file's property handler, even if it means

            // retrieving the file from offline storage.
            const val GPS_DELAYCREATION: Int =
                0x00000020 // delay the creation of the file's property handler until those properties

            // are read, written, or enumerated
            const val GPS_BESTEFFORT: Int =
                0x00000040 // For readonly stores, succeed and return all available properties, even if

            // one or more sources of properties fails. Not valid with int GPS_READWRITE.
            const val GPS_NO_OPLOCK: Int =
                0x00000080 // some data sources protect the read property store with an oplock, this

            // disables that
            const val GPS_MASK_VALID: Int = 0x000000FF
        }
    }

    @Suppress("ktlint:standard:property-naming")
    interface SIGDN {
        companion object {
            var SIGDN_NORMALDISPLAY: Long = 0
            var SIGDN_PARENTRELATIVEPARSING: Long = 0x80018001
            var SIGDN_DESKTOPABSOLUTEPARSING: Long = 0x80028000
            var SIGDN_PARENTRELATIVEEDITING: Long = 0x80031001
            var SIGDN_DESKTOPABSOLUTEEDITING: Long = 0x8004c000
            var SIGDN_FILESYSPATH: Long = 0x80058000
            var SIGDN_URL: Long = 0x80068000
            var SIGDN_PARENTRELATIVEFORADDRESSBAR: Long = 0x8007c001
            var SIGDN_PARENTRELATIVE: Long = 0x80080001
            var SIGDN_PARENTRELATIVEFORUI: Long = 0x80094001
        }
    }

    interface SICHINTF {
        companion object {
            const val SICHINT_DISPLAY: Long = 0
            const val SICHINT_ALLFIELDS: Long = 0x80000000
            const val SICHINT_CANONICAL: Long = 0x10000000
            const val SICHINT_TEST_FILESYSPATH_IF_NOT_EQUAL: Long = 0x20000000
        }
    }
}
