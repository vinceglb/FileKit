package io.github.vinceglb.filekit.sample.shared.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import filekit.sample.shared.generated.resources.GeistMono_Black
import filekit.sample.shared.generated.resources.GeistMono_Bold
import filekit.sample.shared.generated.resources.GeistMono_ExtraBold
import filekit.sample.shared.generated.resources.GeistMono_ExtraLight
import filekit.sample.shared.generated.resources.GeistMono_Light
import filekit.sample.shared.generated.resources.GeistMono_Medium
import filekit.sample.shared.generated.resources.GeistMono_Regular
import filekit.sample.shared.generated.resources.GeistMono_SemiBold
import filekit.sample.shared.generated.resources.GeistMono_Thin
import filekit.sample.shared.generated.resources.Geist_Black
import filekit.sample.shared.generated.resources.Geist_Bold
import filekit.sample.shared.generated.resources.Geist_ExtraBold
import filekit.sample.shared.generated.resources.Geist_ExtraLight
import filekit.sample.shared.generated.resources.Geist_Light
import filekit.sample.shared.generated.resources.Geist_Medium
import filekit.sample.shared.generated.resources.Geist_Regular
import filekit.sample.shared.generated.resources.Geist_SemiBold
import filekit.sample.shared.generated.resources.Geist_Thin
import filekit.sample.shared.generated.resources.Res
import org.jetbrains.compose.resources.Font

@Composable
internal fun geistFontFamily(): FontFamily = FontFamily(
    Font(Res.font.Geist_Black, weight = FontWeight.Black),
    Font(Res.font.Geist_ExtraBold, weight = FontWeight.ExtraBold),
    Font(Res.font.Geist_Bold, weight = FontWeight.Bold),
    Font(Res.font.Geist_SemiBold, weight = FontWeight.SemiBold),
    Font(Res.font.Geist_Medium, weight = FontWeight.Medium),
    Font(Res.font.Geist_Regular, weight = FontWeight.Normal),
    Font(Res.font.Geist_Light, weight = FontWeight.Light),
    Font(Res.font.Geist_ExtraLight, weight = FontWeight.ExtraLight),
    Font(Res.font.Geist_Thin, weight = FontWeight.Thin),
)

@Composable
internal fun geistMonoFontFamily(): FontFamily = FontFamily(
    Font(Res.font.GeistMono_Black, weight = FontWeight.Black),
    Font(Res.font.GeistMono_ExtraBold, weight = FontWeight.ExtraBold),
    Font(Res.font.GeistMono_Bold, weight = FontWeight.Bold),
    Font(Res.font.GeistMono_SemiBold, weight = FontWeight.SemiBold),
    Font(Res.font.GeistMono_Medium, weight = FontWeight.Medium),
    Font(Res.font.GeistMono_Regular, weight = FontWeight.Normal),
    Font(Res.font.GeistMono_Light, weight = FontWeight.Light),
    Font(Res.font.GeistMono_ExtraLight, weight = FontWeight.ExtraLight),
    Font(Res.font.GeistMono_Thin, weight = FontWeight.Thin),
)
