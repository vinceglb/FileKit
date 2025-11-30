package io.github.vinceglb.filekit.convention

import org.gradle.api.Project
import org.gradle.kotlin.dsl.support.uppercaseFirstChar

private const val basePackage = "io.github.vinceglb.filekit"

val Project.modulePackage: String
    get() = "$basePackage${path.replace(":", ".")}"
        .replace("filekit-", "")
        .replace("-", ".")

val Project.moduleName: String
    get() = path
        .split(":").filter { it.isNotEmpty() }.joinToString(separator = "") { it.uppercaseFirstChar() }
        .split("-").filter { it.isNotEmpty() }.joinToString(separator = "") { it.uppercaseFirstChar() }
        .replace("Filekit", "FileKit")
        .let {
            when (it) {
                "FileKitCore" -> "FileKit"
                else -> it
            }
        }
