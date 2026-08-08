package io.github.vinceglb.filekit.dialogs.compose.compatibility;

import androidx.compose.runtime.Composer;
import io.github.vinceglb.filekit.dialogs.FileKitShareSettings;
import io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_mobileKt;

/**
 * Source for the class fixture in androidHostTest/resources. Compile this source only against the
 * fixed-point FileKit artifacts so the runtime test proves that precompiled legacy sharing consumers
 * still link.
 */
public final class LegacySharingLauncherConsumer {
    private LegacySharingLauncherConsumer() {}

    public static void linkLegacyOverload() {
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
