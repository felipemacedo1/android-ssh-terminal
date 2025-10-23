package com.ktar.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ktar.R

/**
 * Terminal-style header for KTAR branding.
 */
@Composable
fun KTARHeader(
    modifier: Modifier = Modifier,
    backgroundColor: Color = Color(0xFF0A0F14),
    textColor: Color = Color(0xFF5CFCCC)
) {
    val jetBrainsMono = FontFamily(
        Font(R.font.jetbrains_mono_regular, FontWeight.Normal),
        Font(R.font.jetbrains_mono_bold, FontWeight.Bold)
    )
    
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .padding(vertical = 24.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = ">_KTAR",
                fontFamily = jetBrainsMono,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = textColor,
                letterSpacing = 2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "in a SSH connection.",
                fontFamily = jetBrainsMono,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = textColor.copy(alpha = 0.8f),
                letterSpacing = 1.sp
            )
        }
    }
}
