package io.github.vinceglb.filekit.sample.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import filekit.sample.composeapp.generated.resources.GeistMono_Black
import filekit.sample.composeapp.generated.resources.GeistMono_Bold
import filekit.sample.composeapp.generated.resources.GeistMono_ExtraBold
import filekit.sample.composeapp.generated.resources.GeistMono_ExtraLight
import filekit.sample.composeapp.generated.resources.GeistMono_Light
import filekit.sample.composeapp.generated.resources.GeistMono_Medium
import filekit.sample.composeapp.generated.resources.GeistMono_Regular
import filekit.sample.composeapp.generated.resources.GeistMono_SemiBold
import filekit.sample.composeapp.generated.resources.GeistMono_Thin
import filekit.sample.composeapp.generated.resources.Geist_Black
import filekit.sample.composeapp.generated.resources.Geist_Bold
import filekit.sample.composeapp.generated.resources.Geist_ExtraBold
import filekit.sample.composeapp.generated.resources.Geist_ExtraLight
import filekit.sample.composeapp.generated.resources.Geist_Light
import filekit.sample.composeapp.generated.resources.Geist_Medium
import filekit.sample.composeapp.generated.resources.Geist_Regular
import filekit.sample.composeapp.generated.resources.Geist_SemiBold
import filekit.sample.composeapp.generated.resources.Geist_Thin
import filekit.sample.composeapp.generated.resources.Res

@Composable
fun geistFontFamily(): FontFamily = FontFamily(
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_Black, weight = FontWeight.Black),
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_ExtraBold, weight = FontWeight.ExtraBold),
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_Bold, weight = FontWeight.Bold),
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_SemiBold, weight = FontWeight.SemiBold),
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_Medium, weight = FontWeight.Medium),
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_Regular, weight = FontWeight.Normal),
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_Light, weight = FontWeight.Light),
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_ExtraLight, weight = FontWeight.ExtraLight),
    org.jetbrains.compose.resources
        .Font(Res.font.Geist_Thin, weight = FontWeight.Thin),
)

@Composable
fun geistMonoFontFamily(): FontFamily = FontFamily(
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_Black, weight = FontWeight.Black),
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_ExtraBold, weight = FontWeight.ExtraBold),
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_Bold, weight = FontWeight.Bold),
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_SemiBold, weight = FontWeight.SemiBold),
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_Medium, weight = FontWeight.Medium),
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_Regular, weight = FontWeight.Normal),
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_Light, weight = FontWeight.Light),
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_ExtraLight, weight = FontWeight.ExtraLight),
    org.jetbrains.compose.resources
        .Font(Res.font.GeistMono_Thin, weight = FontWeight.Thin),
)
