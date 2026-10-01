package com.example.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.IslamicGreenDark
import com.example.ui.theme.IslamicGreenLight
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
  onTimeout: () -> Unit
) {
  val scale = remember { Animatable(0.8f) }
  val alpha = remember { Animatable(0f) }

  LaunchedEffect(key1 = true) {
    scale.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing)
    )
    alpha.animateTo(
      targetValue = 1f,
      animationSpec = tween(durationMillis = 800)
    )
    delay(1200)
    onTimeout()
  }

  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        brush = Brush.verticalGradient(
          colors = listOf(IslamicGreenLight, IslamicGreenDark, Color(0xFF06281E))
        )
      ),
    contentAlignment = Alignment.Center
  ) {
    // Center Content: Logo & Titles
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center,
      modifier = Modifier
        .scale(scale.value)
        .alpha(alpha.value)
        .padding(24.dp)
    ) {
      Surface(
        shape = CircleShape,
        color = Color(0xFF111827),
        shadowElevation = 8.dp,
        modifier = Modifier.size(90.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Text(
            text = "नात",
            color = GoldAccent,
            fontWeight = FontWeight.Bold,
            fontSize = 28.sp
          )
        }
      }

      Spacer(modifier = Modifier.height(24.dp))

      Text(
        text = "Naat Sharif & Kalam Sharif",
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Hindi • Gujarati • Urdu Lyrics",
        style = MaterialTheme.typography.bodyMedium,
        color = GoldAccent,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center
      )
    }

    // Bottom Footer Credit: Developed By Azazmadkiya
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .padding(bottom = 36.dp)
        .alpha(alpha.value),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Developed By",
        style = MaterialTheme.typography.labelSmall,
        color = Color.White.copy(alpha = 0.7f),
        letterSpacing = 1.sp
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Azazmadkiya",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = GoldAccent,
          letterSpacing = 0.5.sp
        )
        Spacer(modifier = Modifier.width(6.dp))
        Icon(
          imageVector = Icons.Default.Favorite,
          contentDescription = null,
          tint = Color(0xFFE53935),
          modifier = Modifier.size(16.dp)
        )
      }
    }
  }
}
