package io.github.vinceglb.filekit.dialogs.compose.compatibility;

import androidx.compose.runtime.Composer;
import io.github.vinceglb.filekit.PlatformFile;
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings;
import io.github.vinceglb.filekit.dialogs.FileKitMode;
import io.github.vinceglb.filekit.dialogs.FileKitType;
import io.github.vinceglb.filekit.dialogs.compose.FileKitComposeKt;
import io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_jvmKt;
import io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_nonAndroidKt;
import io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_nonWebKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Source for the class fixture in jvmTest/resources. Compile this source only against the fixed-point
 * FileKit artifacts so the runtime test proves that precompiled legacy consumers still link.
 */
public final class LegacyPickerLauncherConsumer {
    private LegacyPickerLauncherConsumer() {}

    public static void linkLegacyOverloads() {
        link(() -> FileKitComposeKt.<Object, Object>rememberFilePickerLauncher(
            (FileKitType) null,
            (FileKitMode<Object, Object>) null,
            (PlatformFile) null,
            (FileKitDialogSettings) null,
            (Function1<Object, Unit>) null,
            (Composer) null,
            0,
            0
        ));
        link(() -> FileKitComposeKt.rememberFilePickerLauncher(
            (FileKitType) null,
            (PlatformFile) null,
            (FileKitDialogSettings) null,
            (Function1<PlatformFile, Unit>) null,
            (Composer) null,
            0,
            0
        ));
        link(() -> FileKitCompose_nonAndroidKt.rememberDirectoryPickerLauncher(
            (PlatformFile) null,
            (FileKitDialogSettings) null,
            (Function1<PlatformFile, Unit>) null,
            (Composer) null,
            0,
            0
        ));
        link(() -> FileKitCompose_jvmKt.rememberDirectoryPickerLauncher(
            null,
            (PlatformFile) null,
            (FileKitDialogSettings) null,
            (Function1<PlatformFile, Unit>) null,
            (Composer) null,
            0,
            0
        ));
        link(() -> FileKitCompose_nonWebKt.rememberFileSaverLauncher(
            (FileKitDialogSettings) null,
            (Function1<PlatformFile, Unit>) null,
            (Composer) null,
            0
        ));
        link(() -> FileKitCompose_jvmKt.rememberFileSaverLauncher(
            null,
            (FileKitDialogSettings) null,
            (Function1<PlatformFile, Unit>) null,
            (Composer) null,
            0,
            0
        ));
    }

    private static void link(LinkageCall call) {
        try {
            call.invoke();
        } catch (LinkageError failure) {
            throw failure;
        } catch (Throwable expectedEntryFailure) {
            // Null arguments are intentional: reaching the entry point proves method resolution.
        }
    }

    private interface LinkageCall {
        void invoke();
    }
}
