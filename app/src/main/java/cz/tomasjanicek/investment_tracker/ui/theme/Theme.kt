package cz.tomasjanicek.investment_tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = AppColors.MidnightNavy,
    secondary = AppColors.MutedGold,
    tertiary = AppColors.ProfitGreen,
    surface = AppColors.MidnightNavy,
    background = AppColors.MidnightNavy,
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onSurface = Color.White,
    onBackground = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = AppColors.MidnightNavy,
    secondary = AppColors.MutedGold,
    tertiary = AppColors.ProfitGreen,
    surface = AppColors.ParchmentSurface,
    background = AppColors.ParchmentSurface,
    surfaceVariant = AppColors.PeriwinkleBlue,
    onPrimary = Color.White,
    onSecondary = AppColors.MidnightNavy, // Tmavě modrý text na zlaté
    onSurface = AppColors.MidnightNavy,
    onBackground = AppColors.MidnightNavy,
    onSurfaceVariant = AppColors.MidnightNavy,
    error = AppColors.LossRed
)

@Composable
fun Investment_TrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is disabled by default to show our custom palette
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
