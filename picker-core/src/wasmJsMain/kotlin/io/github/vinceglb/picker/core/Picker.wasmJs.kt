package io.github.vinceglb.picker.core

import kotlinx.browser.document
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.asList
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

public actual object Picker {
	public actual suspend fun <Out> pick(
		mode: PickerSelectionMode<Out>,
		title: String?,
		initialDirectory: String?
	): Out? = withContext(Dispatchers.Default) {
		suspendCoroutine { continuation ->
			// Create input element
			val input = document.createElement("input") as HTMLInputElement

			// Configure the input element
			input.configure(mode)

			// Setup the change listener
			input.onchange = { event ->
				print("onchange")

				try {
					// Get the selected files
					val files = event.target
						?.unsafeCast<HTMLInputElement>()
						?.files
						?.asList()

					// Return the result
					val selection = PickerSelectionMode.SelectionResult(files)
					continuation.resume(mode.result(selection))
				} catch (e: Throwable) {
					continuation.resumeWithException(e)
				}
			}

			input.oncancel = {
				continuation.resume(null)
			}

			// Trigger the file picker
			input.click()
		}
	}

	private fun HTMLInputElement.configure(
		mode: PickerSelectionMode<*>,
	): HTMLInputElement {
		type = "file"

		when (mode) {
			is PickerSelectionMode.SingleFile -> {
				// Set the allowed file types
				mode.extensions?.let {
					accept = mode.extensions.joinToString(",") { ".$it" }
				}

				// Allow only one file
				multiple = false
			}

			is PickerSelectionMode.MultipleFiles -> {
				// Set the allowed file types
				mode.extensions?.let {
					accept = mode.extensions.joinToString(",") { ".$it" }
				}

				// Allow multiple files
				multiple = true
			}

			PickerSelectionMode.Directory ->
				throw NotImplementedError("Directory selection is not supported on the web")

			else ->
				throw IllegalArgumentException("Unsupported mode: $mode")
		}

		return this
	}
}
