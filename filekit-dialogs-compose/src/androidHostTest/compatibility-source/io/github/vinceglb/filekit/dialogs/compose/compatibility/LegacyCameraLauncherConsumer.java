package io.github.vinceglb.filekit.dialogs.compose.compatibility;

import androidx.compose.runtime.Composer;
import io.github.vinceglb.filekit.PlatformFile;
import io.github.vinceglb.filekit.dialogs.FileKitOpenCameraSettings;
import io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_androidKt;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;

/**
 * Source for the class fixture in androidHostTest/resources. Compile this source only against the
 * fixed-point FileKit artifacts so the runtime test proves that precompiled legacy camera consumers
 * still link.
 */
public final class LegacyCameraLauncherConsumer {
    private LegacyCameraLauncherConsumer() {}

    public static void linkLegacyOverload() {
        link(() -> FileKitCompose_androidKt.rememberCameraPickerLauncher(
            (FileKitOpenCameraSettings) null,
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
