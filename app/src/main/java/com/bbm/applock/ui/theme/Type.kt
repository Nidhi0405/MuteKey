package com.bbm.applock.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.bbm.applock.R


val HalantFontFamily = FontFamily(
    Font(R.font.halant_light, FontWeight.Light),
    Font(R.font.halant_regular, FontWeight.Normal),
    Font(R.font.halant_medium, FontWeight.Medium),
    Font(R.font.halant_semibold, FontWeight.SemiBold),
    Font(R.font.halant_bold, FontWeight.Bold),
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HalantFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 32.sp
    ),
    titleLarge = TextStyle(
        fontFamily = HalantFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = HalantFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 18.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = HalantFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = HalantFontFamily,
        fontWeight = FontWeight.Light,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = HalantFontFamily,
        fontWeight = FontWeight.W300,
        fontSize = 12.sp,
        color = Color(0xFF232323)
    )
)
