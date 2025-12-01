package com.example.compose
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import backgroundDark
import backgroundDarkHighContrast
import backgroundDarkMediumContrast
import backgroundLight
import backgroundLightHighContrast
import backgroundLightMediumContrast
import errorContainerDark
import errorContainerDarkHighContrast
import errorContainerDarkMediumContrast
import errorContainerLight
import errorContainerLightHighContrast
import errorContainerLightMediumContrast
import errorDark
import errorDarkHighContrast
import errorDarkMediumContrast
import errorLight
import errorLightHighContrast
import errorLightMediumContrast
import inverseOnSurfaceDark
import inverseOnSurfaceDarkHighContrast
import inverseOnSurfaceDarkMediumContrast
import inverseOnSurfaceLight
import inverseOnSurfaceLightHighContrast
import inverseOnSurfaceLightMediumContrast
import inversePrimaryDark
import inversePrimaryDarkHighContrast
import inversePrimaryDarkMediumContrast
import inversePrimaryLight
import inversePrimaryLightHighContrast
import inversePrimaryLightMediumContrast
import inverseSurfaceDark
import inverseSurfaceDarkHighContrast
import inverseSurfaceDarkMediumContrast
import inverseSurfaceLight
import inverseSurfaceLightHighContrast
import inverseSurfaceLightMediumContrast
import onBackgroundDark
import onBackgroundDarkHighContrast
import onBackgroundDarkMediumContrast
import onBackgroundLight
import onBackgroundLightHighContrast
import onBackgroundLightMediumContrast
import onErrorContainerDark
import onErrorContainerDarkHighContrast
import onErrorContainerDarkMediumContrast
import onErrorContainerLight
import onErrorContainerLightHighContrast
import onErrorContainerLightMediumContrast
import onErrorDark
import onErrorDarkHighContrast
import onErrorDarkMediumContrast
import onErrorLight
import onErrorLightHighContrast
import onErrorLightMediumContrast
import onPrimaryContainerDark
import onPrimaryContainerDarkHighContrast
import onPrimaryContainerDarkMediumContrast
import onPrimaryContainerLight
import onPrimaryContainerLightHighContrast
import onPrimaryContainerLightMediumContrast
import onPrimaryDark
import onPrimaryDarkHighContrast
import onPrimaryDarkMediumContrast
import onPrimaryLight
import onPrimaryLightHighContrast
import onPrimaryLightMediumContrast
import onSecondaryContainerDark
import onSecondaryContainerDarkHighContrast
import onSecondaryContainerDarkMediumContrast
import onSecondaryContainerLight
import onSecondaryContainerLightHighContrast
import onSecondaryContainerLightMediumContrast
import onSecondaryDark
import onSecondaryDarkHighContrast
import onSecondaryDarkMediumContrast
import onSecondaryLight
import onSecondaryLightHighContrast
import onSecondaryLightMediumContrast
import onSurfaceDark
import onSurfaceDarkHighContrast
import onSurfaceDarkMediumContrast
import onSurfaceLight
import onSurfaceLightHighContrast
import onSurfaceLightMediumContrast
import onSurfaceVariantDark
import onSurfaceVariantDarkHighContrast
import onSurfaceVariantDarkMediumContrast
import onSurfaceVariantLight
import onSurfaceVariantLightHighContrast
import onSurfaceVariantLightMediumContrast
import onTertiaryContainerDark
import onTertiaryContainerDarkHighContrast
import onTertiaryContainerDarkMediumContrast
import onTertiaryContainerLight
import onTertiaryContainerLightHighContrast
import onTertiaryContainerLightMediumContrast
import onTertiaryDark
import onTertiaryDarkHighContrast
import onTertiaryDarkMediumContrast
import onTertiaryLight
import onTertiaryLightHighContrast
import onTertiaryLightMediumContrast
import outlineDark
import outlineDarkHighContrast
import outlineDarkMediumContrast
import outlineLight
import outlineLightHighContrast
import outlineLightMediumContrast
import outlineVariantDark
import outlineVariantDarkHighContrast
import outlineVariantDarkMediumContrast
import outlineVariantLight
import outlineVariantLightHighContrast
import outlineVariantLightMediumContrast
import primaryContainerDark
import primaryContainerDarkHighContrast
import primaryContainerDarkMediumContrast
import primaryContainerLight
import primaryContainerLightHighContrast
import primaryContainerLightMediumContrast
import primaryDark
import primaryDarkHighContrast
import primaryDarkMediumContrast
import primaryLight
import primaryLightHighContrast
import primaryLightMediumContrast
import scrimDark
import scrimDarkHighContrast
import scrimDarkMediumContrast
import scrimLight
import scrimLightHighContrast
import scrimLightMediumContrast
import secondaryContainerDark
import secondaryContainerDarkHighContrast
import secondaryContainerDarkMediumContrast
import secondaryContainerLight
import secondaryContainerLightHighContrast
import secondaryContainerLightMediumContrast
import secondaryDark
import secondaryDarkHighContrast
import secondaryDarkMediumContrast
import secondaryLight
import secondaryLightHighContrast
import secondaryLightMediumContrast
import surfaceDark
import surfaceDarkHighContrast
import surfaceDarkMediumContrast
import surfaceLight
import surfaceLightHighContrast
import surfaceLightMediumContrast
import surfaceVariantDark
import surfaceVariantDarkHighContrast
import surfaceVariantDarkMediumContrast
import surfaceVariantLight
import surfaceVariantLightHighContrast
import surfaceVariantLightMediumContrast
import tertiaryContainerDark
import tertiaryContainerDarkHighContrast
import tertiaryContainerDarkMediumContrast
import tertiaryContainerLight
import tertiaryContainerLightHighContrast
import tertiaryContainerLightMediumContrast
import tertiaryDark
import tertiaryDarkHighContrast
import tertiaryDarkMediumContrast
import tertiaryLight
import tertiaryLightHighContrast
import tertiaryLightMediumContrast

private val lightScheme =
  lightColorScheme(
    primary = primaryLight,
    onPrimary = onPrimaryLight,
    primaryContainer = primaryContainerLight,
    onPrimaryContainer = onPrimaryContainerLight,
    secondary = secondaryLight,
    onSecondary = onSecondaryLight,
    secondaryContainer = secondaryContainerLight,
    onSecondaryContainer = onSecondaryContainerLight,
    tertiary = tertiaryLight,
    onTertiary = onTertiaryLight,
    tertiaryContainer = tertiaryContainerLight,
    onTertiaryContainer = onTertiaryContainerLight,
    error = errorLight,
    onError = onErrorLight,
    errorContainer = errorContainerLight,
    onErrorContainer = onErrorContainerLight,
    background = backgroundLight,
    onBackground = onBackgroundLight,
    surface = surfaceLight,
    onSurface = onSurfaceLight,
    surfaceVariant = surfaceVariantLight,
    onSurfaceVariant = onSurfaceVariantLight,
    outline = outlineLight,
    outlineVariant = outlineVariantLight,
    scrim = scrimLight,
    inverseSurface = inverseSurfaceLight,
    inverseOnSurface = inverseOnSurfaceLight,
    inversePrimary = inversePrimaryLight,
  )

private val darkScheme =
  darkColorScheme(
    primary = primaryDark,
    onPrimary = onPrimaryDark,
    primaryContainer = primaryContainerDark,
    onPrimaryContainer = onPrimaryContainerDark,
    secondary = secondaryDark,
    onSecondary = onSecondaryDark,
    secondaryContainer = secondaryContainerDark,
    onSecondaryContainer = onSecondaryContainerDark,
    tertiary = tertiaryDark,
    onTertiary = onTertiaryDark,
    tertiaryContainer = tertiaryContainerDark,
    onTertiaryContainer = onTertiaryContainerDark,
    error = errorDark,
    onError = onErrorDark,
    errorContainer = errorContainerDark,
    onErrorContainer = onErrorContainerDark,
    background = backgroundDark,
    onBackground = onBackgroundDark,
    surface = surfaceDark,
    onSurface = onSurfaceDark,
    surfaceVariant = surfaceVariantDark,
    onSurfaceVariant = onSurfaceVariantDark,
    outline = outlineDark,
    outlineVariant = outlineVariantDark,
    scrim = scrimDark,
    inverseSurface = inverseSurfaceDark,
    inverseOnSurface = inverseOnSurfaceDark,
    inversePrimary = inversePrimaryDark,
  )

private val mediumContrastLightColorScheme =
  lightColorScheme(
    primary = primaryLightMediumContrast,
    onPrimary = onPrimaryLightMediumContrast,
    primaryContainer = primaryContainerLightMediumContrast,
    onPrimaryContainer = onPrimaryContainerLightMediumContrast,
    secondary = secondaryLightMediumContrast,
    onSecondary = onSecondaryLightMediumContrast,
    secondaryContainer = secondaryContainerLightMediumContrast,
    onSecondaryContainer = onSecondaryContainerLightMediumContrast,
    tertiary = tertiaryLightMediumContrast,
    onTertiary = onTertiaryLightMediumContrast,
    tertiaryContainer = tertiaryContainerLightMediumContrast,
    onTertiaryContainer = onTertiaryContainerLightMediumContrast,
    error = errorLightMediumContrast,
    onError = onErrorLightMediumContrast,
    errorContainer = errorContainerLightMediumContrast,
    onErrorContainer = onErrorContainerLightMediumContrast,
    background = backgroundLightMediumContrast,
    onBackground = onBackgroundLightMediumContrast,
    surface = surfaceLightMediumContrast,
    onSurface = onSurfaceLightMediumContrast,
    surfaceVariant = surfaceVariantLightMediumContrast,
    onSurfaceVariant = onSurfaceVariantLightMediumContrast,
    outline = outlineLightMediumContrast,
    outlineVariant = outlineVariantLightMediumContrast,
    scrim = scrimLightMediumContrast,
    inverseSurface = inverseSurfaceLightMediumContrast,
    inverseOnSurface = inverseOnSurfaceLightMediumContrast,
    inversePrimary = inversePrimaryLightMediumContrast,
  )

private val highContrastLightColorScheme =
  lightColorScheme(
    primary = primaryLightHighContrast,
    onPrimary = onPrimaryLightHighContrast,
    primaryContainer = primaryContainerLightHighContrast,
    onPrimaryContainer = onPrimaryContainerLightHighContrast,
    secondary = secondaryLightHighContrast,
    onSecondary = onSecondaryLightHighContrast,
    secondaryContainer = secondaryContainerLightHighContrast,
    onSecondaryContainer = onSecondaryContainerLightHighContrast,
    tertiary = tertiaryLightHighContrast,
    onTertiary = onTertiaryLightHighContrast,
    tertiaryContainer = tertiaryContainerLightHighContrast,
    onTertiaryContainer = onTertiaryContainerLightHighContrast,
    error = errorLightHighContrast,
    onError = onErrorLightHighContrast,
    errorContainer = errorContainerLightHighContrast,
    onErrorContainer = onErrorContainerLightHighContrast,
    background = backgroundLightHighContrast,
    onBackground = onBackgroundLightHighContrast,
    surface = surfaceLightHighContrast,
    onSurface = onSurfaceLightHighContrast,
    surfaceVariant = surfaceVariantLightHighContrast,
    onSurfaceVariant = onSurfaceVariantLightHighContrast,
    outline = outlineLightHighContrast,
    outlineVariant = outlineVariantLightHighContrast,
    scrim = scrimLightHighContrast,
    inverseSurface = inverseSurfaceLightHighContrast,
    inverseOnSurface = inverseOnSurfaceLightHighContrast,
    inversePrimary = inversePrimaryLightHighContrast,
  )

private val mediumContrastDarkColorScheme =
  darkColorScheme(
    primary = primaryDarkMediumContrast,
    onPrimary = onPrimaryDarkMediumContrast,
    primaryContainer = primaryContainerDarkMediumContrast,
    onPrimaryContainer = onPrimaryContainerDarkMediumContrast,
    secondary = secondaryDarkMediumContrast,
    onSecondary = onSecondaryDarkMediumContrast,
    secondaryContainer = secondaryContainerDarkMediumContrast,
    onSecondaryContainer = onSecondaryContainerDarkMediumContrast,
    tertiary = tertiaryDarkMediumContrast,
    onTertiary = onTertiaryDarkMediumContrast,
    tertiaryContainer = tertiaryContainerDarkMediumContrast,
    onTertiaryContainer = onTertiaryContainerDarkMediumContrast,
    error = errorDarkMediumContrast,
    onError = onErrorDarkMediumContrast,
    errorContainer = errorContainerDarkMediumContrast,
    onErrorContainer = onErrorContainerDarkMediumContrast,
    background = backgroundDarkMediumContrast,
    onBackground = onBackgroundDarkMediumContrast,
    surface = surfaceDarkMediumContrast,
    onSurface = onSurfaceDarkMediumContrast,
    surfaceVariant = surfaceVariantDarkMediumContrast,
    onSurfaceVariant = onSurfaceVariantDarkMediumContrast,
    outline = outlineDarkMediumContrast,
    outlineVariant = outlineVariantDarkMediumContrast,
    scrim = scrimDarkMediumContrast,
    inverseSurface = inverseSurfaceDarkMediumContrast,
    inverseOnSurface = inverseOnSurfaceDarkMediumContrast,
    inversePrimary = inversePrimaryDarkMediumContrast,
  )

private val highContrastDarkColorScheme =
  darkColorScheme(
    primary = primaryDarkHighContrast,
    onPrimary = onPrimaryDarkHighContrast,
    primaryContainer = primaryContainerDarkHighContrast,
    onPrimaryContainer = onPrimaryContainerDarkHighContrast,
    secondary = secondaryDarkHighContrast,
    onSecondary = onSecondaryDarkHighContrast,
    secondaryContainer = secondaryContainerDarkHighContrast,
    onSecondaryContainer = onSecondaryContainerDarkHighContrast,
    tertiary = tertiaryDarkHighContrast,
    onTertiary = onTertiaryDarkHighContrast,
    tertiaryContainer = tertiaryContainerDarkHighContrast,
    onTertiaryContainer = onTertiaryContainerDarkHighContrast,
    error = errorDarkHighContrast,
    onError = onErrorDarkHighContrast,
    errorContainer = errorContainerDarkHighContrast,
    onErrorContainer = onErrorContainerDarkHighContrast,
    background = backgroundDarkHighContrast,
    onBackground = onBackgroundDarkHighContrast,
    surface = surfaceDarkHighContrast,
    onSurface = onSurfaceDarkHighContrast,
    surfaceVariant = surfaceVariantDarkHighContrast,
    onSurfaceVariant = onSurfaceVariantDarkHighContrast,
    outline = outlineDarkHighContrast,
    outlineVariant = outlineVariantDarkHighContrast,
    scrim = scrimDarkHighContrast,
    inverseSurface = inverseSurfaceDarkHighContrast,
    inverseOnSurface = inverseOnSurfaceDarkHighContrast,
    inversePrimary = inversePrimaryDarkHighContrast,
  )

@Immutable
data class ColorFamily(
  val color: Color,
  val onColor: Color,
  val colorContainer: Color,
  val onColorContainer: Color,
)

val unspecified_scheme =
  ColorFamily(
    Color.Unspecified,
    Color.Unspecified,
    Color.Unspecified,
    Color.Unspecified,
  )

@Composable
fun AppTheme(
  darkTheme: Boolean = isSystemInDarkTheme(),
  content:
    @Composable()
    () -> Unit,
) {
  val colorScheme =
    when {
      darkTheme -> darkScheme
      else -> lightScheme
    }

  MaterialTheme(
    colorScheme = colorScheme,
    content = content,
  )
}
