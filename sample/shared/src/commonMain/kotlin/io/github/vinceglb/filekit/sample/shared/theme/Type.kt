package io.github.vinceglb.filekit.sample.shared.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable

@Composable
internal fun geistTypography(): Typography {
    val geistFont = geistFontFamily()

    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(fontFamily = geistFont),
            displayMedium = displayMedium.copy(fontFamily = geistFont),
            displaySmall = displaySmall.copy(fontFamily = geistFont),
            headlineLarge = headlineLarge.copy(fontFamily = geistFont),
            headlineMedium = headlineMedium.copy(fontFamily = geistFont),
            headlineSmall = headlineSmall.copy(fontFamily = geistFont),
            titleLarge = titleLarge.copy(fontFamily = geistFont),
            titleMedium = titleMedium.copy(fontFamily = geistFont),
            titleSmall = titleSmall.copy(fontFamily = geistFont),
            labelLarge = labelLarge.copy(fontFamily = geistFont),
            labelMedium = labelMedium.copy(fontFamily = geistFont),
            labelSmall = labelSmall.copy(fontFamily = geistFont),
            bodyLarge = bodyLarge.copy(fontFamily = geistFont),
            bodyMedium = bodyMedium.copy(fontFamily = geistFont),
            bodySmall = bodySmall.copy(fontFamily = geistFont),
        )
    }
}

@Composable
internal fun geistMonoTypography(): Typography {
    val geistMonoFont = geistMonoFontFamily()

    return with(MaterialTheme.typography) {
        copy(
            displayLarge = displayLarge.copy(fontFamily = geistMonoFont),
            displayMedium = displayMedium.copy(fontFamily = geistMonoFont),
            displaySmall = displaySmall.copy(fontFamily = geistMonoFont),
            headlineLarge = headlineLarge.copy(fontFamily = geistMonoFont),
            headlineMedium = headlineMedium.copy(fontFamily = geistMonoFont),
            headlineSmall = headlineSmall.copy(fontFamily = geistMonoFont),
            titleLarge = titleLarge.copy(fontFamily = geistMonoFont),
            titleMedium = titleMedium.copy(fontFamily = geistMonoFont),
            titleSmall = titleSmall.copy(fontFamily = geistMonoFont),
            labelLarge = labelLarge.copy(fontFamily = geistMonoFont),
            labelMedium = labelMedium.copy(fontFamily = geistMonoFont),
            labelSmall = labelSmall.copy(fontFamily = geistMonoFont),
            bodyLarge = bodyLarge.copy(fontFamily = geistMonoFont),
            bodyMedium = bodyMedium.copy(fontFamily = geistMonoFont),
            bodySmall = bodySmall.copy(fontFamily = geistMonoFont),
        )
    }
}
