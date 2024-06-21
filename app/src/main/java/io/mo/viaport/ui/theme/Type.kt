package io.mo.viaport.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Define and load the fonts of the app
// private val light = Font(R.font.raleway_light, FontWeight.W300)
// private val regular = Font(R.font.raleway_regular, FontWeight.W400)
// private val medium = Font(R.font.raleway_medium, FontWeight.W500)
// private val semibold = Font(R.font.raleway_semibold, FontWeight.W600)
// // Create a font family to use in TextStyles
// private val craneFontFamily = FontFamily(light, regular, medium, semibold)


// Use the font family to define a custom typography
// Set of Material typography styles to start with
val Typography2 = androidx.compose.material.Typography(
    // defaultFontFamily = craneFontFamily,
    body1 = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),

    button = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp
    ),
    caption = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    )
)
// Set of Material typography styles to start with
val Typography = androidx.compose.material3.Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )

    /* Other default text styles to override
    */
)