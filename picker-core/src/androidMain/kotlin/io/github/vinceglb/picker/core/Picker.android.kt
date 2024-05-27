package io.github.vinceglb.picker.core

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageAndVideo
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
import androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.VideoOnly
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public actual object Picker {
    private var registry: ActivityResultRegistry? = null
    private var context: WeakReference<Context?> = WeakReference(null)

    public fun init(activity: ComponentActivity) {
        context = WeakReference(activity.applicationContext)
        registry = activity.activityResultRegistry
    }

    public actual suspend fun <Out> pickFile(
        type: PickerSelectionType,
        mode: PickerSelectionMode<Out>,
        title: String?,
        initialDirectory: String?,
        platformSettings: PickerPlatformSettings?,
    ): Out? = withContext(Dispatchers.IO) {
        // Throw exception if registry is not initialized
        val registry = registry ?: throw PickerNotInitializedException()

        // It doesn't really matter what the key is, just that it is unique
        val key = UUID.randomUUID().toString()

        // Get context
        val context = Picker.context.get()
            ?: throw PickerNotInitializedException()

        val result: PlatformFiles? = suspendCoroutine { continuation ->
            when (type) {
                PickerSelectionType.Image,
                PickerSelectionType.Video,
                PickerSelectionType.ImageAndVideo -> {
                    when (mode) {
                        is PickerSelectionMode.Single -> {
                            val contract = PickVisualMedia()
                            val launcher = registry.register(key, contract) { uri ->
                                val result = uri?.let { listOf(PlatformFile(it, context)) }
                                continuation.resume(result)
                            }

                            val request = when (type) {
                                PickerSelectionType.Image -> PickVisualMediaRequest(ImageOnly)
                                PickerSelectionType.Video -> PickVisualMediaRequest(VideoOnly)
                                PickerSelectionType.ImageAndVideo -> PickVisualMediaRequest(ImageAndVideo)
                                else -> throw IllegalArgumentException("Unsupported type: $type")
                            }

                            launcher.launch(request)
                        }

                        is PickerSelectionMode.Multiple -> {
                            val contract = ActivityResultContracts.PickMultipleVisualMedia()
                            val launcher = registry.register(key, contract) { uri ->
                                val result = uri.map { PlatformFile(it, context) }
                                continuation.resume(result)
                            }

                            val request = when (type) {
                                PickerSelectionType.Image -> PickVisualMediaRequest(ImageOnly)
                                PickerSelectionType.Video -> PickVisualMediaRequest(VideoOnly)
                                PickerSelectionType.ImageAndVideo -> PickVisualMediaRequest(ImageAndVideo)
                                else -> throw IllegalArgumentException("Unsupported type: $type")
                            }

                            launcher.launch(request)
                        }
                    }
                }

                is PickerSelectionType.File -> {
                    when (mode) {
                        is PickerSelectionMode.Single -> {
                            val contract = ActivityResultContracts.OpenDocument()
                            val launcher = registry.register(key, contract) { uri ->
                                val result = uri?.let { listOf(PlatformFile(it, context)) }
                                continuation.resume(result)
                            }
                            launcher.launch(getMimeTypes(type.extensions))
                        }

                        is PickerSelectionMode.Multiple -> {
                            val contract = ActivityResultContracts.OpenMultipleDocuments()
                            val launcher = registry.register(key, contract) { uris ->
                                val result = uris.map { PlatformFile(it, context) }
                                continuation.resume(result)
                            }
                            launcher.launch(getMimeTypes(type.extensions))
                        }
                    }
                }
            }
        }

        mode.parseResult(result)
    }

    public actual suspend fun pickDirectory(
        title: String?,
        initialDirectory: String?,
        platformSettings: PickerPlatformSettings?,
    ): PlatformDirectory? = withContext(Dispatchers.IO) {
        // Throw exception if registry is not initialized
        val registry = registry ?: throw PickerNotInitializedException()

        // It doesn't really matter what the key is, just that it is unique
        val key = UUID.randomUUID().toString()

        suspendCoroutine { continuation ->
            val contract = ActivityResultContracts.OpenDocumentTree()
            val launcher = registry.register(key, contract) { uri ->
                val platformDirectory = uri?.let { PlatformDirectory(it) }
                continuation.resume(platformDirectory)
            }
            val initialUri = initialDirectory?.let { Uri.parse(it) }
            launcher.launch(initialUri)
        }
    }

    public actual fun isDirectoryPickerSupported(): Boolean = true

    public actual suspend fun saveFile(
        bytes: ByteArray,
        baseName: String,
        extension: String,
        initialDirectory: String?,
        platformSettings: PickerPlatformSettings?,
    ): PlatformFile? = withContext(Dispatchers.IO) {
        suspendCoroutine { continuation ->
            // Throw exception if registry is not initialized
            val registry = registry ?: throw PickerNotInitializedException()

            // It doesn't really matter what the key is, just that it is unique
            val key = UUID.randomUUID().toString()

            // Get context
            val context = Picker.context.get()
                ?: throw PickerNotInitializedException()

            // Get MIME type
            val mimeType = getMimeType(extension)

            // Create Launcher
            val contract = ActivityResultContracts.CreateDocument(mimeType)
            val launcher = registry.register(key, contract) { uri ->
                val platformFile = uri?.let {
                    // Write the bytes to the file
                    context.contentResolver.openOutputStream(it)?.use { output ->
                        output.write(bytes)
                    }

                    PlatformFile(it, context)
                }
                continuation.resume(platformFile)
            }

            // Launch
            launcher.launch("$baseName.$extension")
        }
    }

    private fun getMimeTypes(fileExtensions: List<String>?): Array<String> {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return fileExtensions
            ?.takeIf { it.isNotEmpty() }
            ?.mapNotNull { mimeTypeMap.getMimeTypeFromExtension(it) }
            ?.toTypedArray()
            ?: arrayOf("*/*")
    }

    private fun getMimeType(fileExtension: String): String {
        val mimeTypeMap = MimeTypeMap.getSingleton()
        return mimeTypeMap.getMimeTypeFromExtension(fileExtension) ?: "*/*"
    }
}
