package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BrandedStampBadge(
    companyName: String = "ZAYED SALESFORCE",
    verificationCode: String,
    dateString: String,
    modifier: Modifier = Modifier,
    stampColor: Color = Color(0xFF0F2942) // Royal Navy Ink
) {
    Box(
        modifier = modifier
            .rotate(-3f)
            .padding(8.dp)
            .border(
                border = BorderStroke(2.5.dp, stampColor),
                shape = RoundedCornerShape(12.dp)
            )
            .background(stampColor.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37), // Imperial Gold
                    modifier = Modifier.size(13.dp)
                )
                Text(
                    text = "  ZAYED SALESFORCE • AL YASIR BIN ZAYED  ",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = stampColor,
                    letterSpacing = 0.8.sp,
                    maxLines = 1
                )
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = Color(0xFFD4AF37),
                    modifier = Modifier.size(13.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Box(
                modifier = Modifier
                    .border(BorderStroke(1.dp, stampColor.copy(alpha = 0.7f)), RoundedCornerShape(6.dp))
                    .padding(horizontal = 10.dp, vertical = 3.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Verified",
                        tint = Color(0xFFD4AF37),
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = " VERIFIED & BOOKED • CLIENT COPY ",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = stampColor,
                        letterSpacing = 0.8.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "DATE: $dateString | REF: $verificationCode",
                fontSize = 9.sp,
                fontWeight = FontWeight.Medium,
                fontFamily = FontFamily.Monospace,
                color = stampColor.copy(alpha = 0.9f)
            )
        }
    }
}
