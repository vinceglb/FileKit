package io.github.vinceglb.filekit.dialogs.compose.compatibility;

import androidx.compose.runtime.Composer;
import io.github.vinceglb.filekit.PlatformFile;
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings;
import io.github.vinceglb.filekit.dialogs.FileKitMode;
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings;
import io.github.vinceglb.filekit.dialogs.FileKitPickerException;
import io.github.vinceglb.filekit.dialogs.FileKitShareSettings;
import io.github.vinceglb.filekit.dialogs.FileKitType;
import io.github.vinceglb.filekit.dialogs.compose.FileKitComposeKt;
import io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_androidKt;
import io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_mobileKt;
import io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_nonWebKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Source for the class fixture in androidHostTest/resources. Compile this source only against the
 * fixed-point FileKit artifacts so the runtime test proves that precompiled consumers of every
 * legacy Android launcher family still link.
 */
public final class LegacyAndroidLauncherConsumer {
    private LegacyAndroidLauncherConsumer() {}

    public static int legacyOverloadCount() {
        return 8;
    }

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
        link(() -> FileKitComposeKt.<Object, Object>rememberFilePickerLauncher(
            (FileKitType) null,
            (FileKitMode<Object, Object>) null,
            (PlatformFile) null,
            (FileKitDialogSettings) null,
            (Function1<FileKitPickerException, Unit>) null,
            (Function1<Object, Unit>) null,
            (Composer) null,
            0,
            0
        ));
        link(() -> FileKitComposeKt.rememberFilePickerLauncher(
            (FileKitType) null,
            (PlatformFile) null,
            (FileKitDialogSettings) null,
            (Function1<FileKitPickerException, Unit>) null,
            (Function1<PlatformFile, Unit>) null,
            (Composer) null,
            0,
            0
        ));
        link(() -> FileKitCompose_androidKt.rememberDirectoryPickerLauncher(
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
        link(() -> FileKitCompose_androidKt.rememberCameraPickerLauncher(
            (FileKitOpenCameraSettings) null,
            (Function1<PlatformFile, Unit>) null,
            (Composer) null,
            0,
            0
        ));
        link(() -> FileKitCompose_mobileKt.rememberShareFileLauncher(
            (FileKitShareSettings) null,
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
