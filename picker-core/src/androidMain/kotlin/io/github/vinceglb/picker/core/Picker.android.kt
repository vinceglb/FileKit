package io.github.vinceglb.picker.core

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.contract.ActivityResultContracts
import io.github.vinceglb.picker.core.PickerSelectionMode.SelectionResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.lang.ref.WeakReference
import java.util.UUID
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

public actual object Picker {
	private var registry: ActivityResultRegistry? = null
	internal var context: WeakReference<Context?> = WeakReference(null)
		private set

	public fun init(activity: ComponentActivity) {
		context = WeakReference(activity.applicationContext)
		registry = activity.activityResultRegistry
	}

	public actual suspend fun <Out> pick(
		mode: PickerSelectionMode<Out>,
		title: String?,
		initialDirectory: String?,
	): Out? = withContext(Dispatchers.IO) {
		// Throw exception if registry is not initialized
		val registry = registry ?: throw IllegalStateException("Picker not initialized")

		// It doesn't really matter what the key is, just that it is unique
		val key = UUID.randomUUID().toString()

		// Open native file picker
		val selection = suspendCoroutine { continuation ->
			when (mode) {
				is PickerSelectionMode.SingleFile -> {
					val contract = ActivityResultContracts.OpenDocument()
					val launcher = registry.register(key, contract) { uri ->
						continuation.resume(SelectionResult(
							files = uri?.let { listOf(it) }
						))
					}
					launcher.launch(getMimeType(mode.extensions))
				}

				is PickerSelectionMode.MultipleFiles -> {
					val contract = ActivityResultContracts.OpenMultipleDocuments()
					val launcher = registry.register(key, contract) { uris ->
						continuation.resume(SelectionResult(files = uris))
					}
					launcher.launch(getMimeType(mode.extensions))
				}

				is PickerSelectionMode.Directory -> {
					val contract = ActivityResultContracts.OpenDocumentTree()
					val launcher = registry.register(key, contract) { uri ->
						continuation.resume(SelectionResult(
							files = uri?.let { listOf(it) }
						))
					}
					val initialUri = initialDirectory?.let { Uri.parse(it) }
					launcher.launch(initialUri)
				}

				else -> throw IllegalArgumentException("Unsupported mode: $mode")
			}
		}

		// Return result
		return@withContext mode.result(selection)
	}

	public fun getMimeType(fileExtensions: List<String>?): Array<String> {
		val mimeTypeMap = MimeTypeMap.getSingleton()
		return fileExtensions
			?.takeIf { it.isNotEmpty() }
			?.mapNotNull { mimeTypeMap.getMimeTypeFromExtension(it) }
			?.toTypedArray()
			?: arrayOf("*/*")
	}
}
