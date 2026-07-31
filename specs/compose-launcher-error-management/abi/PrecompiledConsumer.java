import androidx.compose.runtime.Composer;
import androidx.compose.ui.window.WindowScope;
import io.github.vinceglb.filekit.PlatformFile;
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings;
import io.github.vinceglb.filekit.dialogs.FileKitPickerException;
import io.github.vinceglb.filekit.exceptions.FileKitException;
import kotlin.jvm.functions.Function1;

public final class PrecompiledConsumer {
    public static void main(String[] args) throws ReflectiveOperationException {
        FileKitException failure = new FileKitPickerException("picker failed");
        if (!"picker failed".equals(failure.getMessage())) {
            throw new AssertionError("Unexpected message: " + failure.getMessage());
        }

        Class<?> nonAndroid = Class.forName(
            "io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_nonAndroidKt"
        );
        nonAndroid.getDeclaredMethod(
            "rememberDirectoryPickerLauncher",
            PlatformFile.class,
            FileKitDialogSettings.class,
            Function1.class,
            Composer.class,
            int.class,
            int.class
        );

        Class<?> nonWeb = Class.forName(
            "io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_nonWebKt"
        );
        nonWeb.getDeclaredMethod(
            "rememberFileSaverLauncher",
            FileKitDialogSettings.class,
            Function1.class,
            Composer.class,
            int.class
        );

        Class<?> jvm = Class.forName(
            "io.github.vinceglb.filekit.dialogs.compose.FileKitCompose_jvmKt"
        );
        jvm.getDeclaredMethod(
            "rememberDirectoryPickerLauncher",
            WindowScope.class,
            PlatformFile.class,
            FileKitDialogSettings.class,
            Function1.class,
            Composer.class,
            int.class,
            int.class
        );
        jvm.getDeclaredMethod(
            "rememberFileSaverLauncher",
            WindowScope.class,
            FileKitDialogSettings.class,
            Function1.class,
            Composer.class,
            int.class,
            int.class
        );
    }
}
