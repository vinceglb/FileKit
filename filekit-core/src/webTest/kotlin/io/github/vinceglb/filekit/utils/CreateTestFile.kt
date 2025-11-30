package io.github.vinceglb.filekit.utils

import io.github.vinceglb.filekit.PlatformFile

expect fun createTestFile(
    name: String,
    content: String,
): PlatformFile
