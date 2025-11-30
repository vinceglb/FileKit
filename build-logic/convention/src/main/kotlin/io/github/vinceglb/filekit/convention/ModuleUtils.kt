package io.github.vinceglb.filekit.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

private const val BASE_PACKAGE = "io.github.vinceglb.filekit"

val Project.modulePackage: String
    get() = "$BASE_PACKAGE${path.replace(":", ".")}"
        .replace("filekit-", "")
        .replace("-", ".")

val Project.moduleName: String
    get() = path
        .uppercaseAfterChar(":")
        .uppercaseAfterChar("-")
        .replace("Filekit", "FileKit")
        .let {
            when (it) {
                "FileKitCore" -> "FileKit"
                else -> it
            }
        }

private fun String.uppercaseAfterChar(delimiter: String): String = this
    .split(":")
    .filter { it.isNotEmpty() }
    .joinToString(separator = "") { it.uppercaseFirstChar() }
