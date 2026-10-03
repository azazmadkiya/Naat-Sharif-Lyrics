package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NaatItem
import com.example.viewmodel.NaatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
  viewModel: NaatViewModel,
  onBack: () -> Unit
) {
  val naat = viewModel.selectedNaat ?: NaatItem(title = "Naat Sharif", lyrics = "No lyrics available.")
  val context = LocalContext.current
  val scrollState = rememberScrollState()

  // Selected Language state: "Hindi" or "Gujarati"
  var selectedLanguage by remember { mutableStateOf("Hindi") }

  val displayText = when (selectedLanguage) {
    "Gujarati" -> when {
      naat.gujaratiLyrics.isNotBlank() -> naat.gujaratiLyrics
      naat.lyrics.isNotBlank() && naat.lyrics != naat.hindiLyrics -> naat.lyrics
      naat.hindiLyrics.isNotBlank() -> naat.hindiLyrics
      else -> "ગુજરાતી લિરિક્સ ઉપલબ્ધ નથી."
    }
    else -> when {
      naat.hindiLyrics.isNotBlank() -> naat.hindiLyrics
      naat.lyrics.isNotBlank() -> naat.lyrics
      naat.gujaratiLyrics.isNotBlank() -> naat.gujaratiLyrics
      else -> "हिन्दी लिरिक्स उपलब्ध नहीं है।"
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(naat.title, maxLines = 1) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { viewModel.isDarkMode = !viewModel.isDarkMode }) {
            Icon(
              imageVector = if (viewModel.isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
              contentDescription = "Toggle Night Mode"
            )
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White,
          actionIconContentColor = Color.White
        )
      )
    },
    bottomBar = {
      Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          horizontalArrangement = Arrangement.SpaceAround,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(onClick = { if (viewModel.fontSizeScale < 32f) viewModel.fontSizeScale += 2f }) {
            Icon(Icons.Default.Add, contentDescription = "Increase Font")
          }
          IconButton(onClick = { if (viewModel.fontSizeScale > 12f) viewModel.fontSizeScale -= 2f }) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease Font")
          }
          IconButton(onClick = { viewModel.toggleFavorite(naat) }) {
            Icon(
              imageVector = if (naat.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
              contentDescription = "Favorite",
              tint = if (naat.isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  ) { padding ->
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(scrollState)
          .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = naat.title,
          style = MaterialTheme.typography.titleLarge,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.primary,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Reciter: ${naat.reciter.ifBlank { "Traditional" }}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))

        // Language Selector Tabs (Hindi vs Gujarati)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
          horizontalArrangement = Arrangement.Center
        ) {
          SingleChoiceSegmentedButtonRow {
            SegmentedButton(
              selected = selectedLanguage == "Hindi",
              onClick = { selectedLanguage = "Hindi" },
              shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
            ) {
              Text("हिन्दी (Hindi)")
            }
            SegmentedButton(
              selected = selectedLanguage == "Gujarati",
              onClick = { selectedLanguage = "Gujarati" },
              shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
            ) {
              Text("ગુજરાતી (Gujarati)")
            }
          }
        }

        Card(
          modifier = Modifier.fillMaxWidth(),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          shape = RoundedCornerShape(16.dp),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = displayText,
              style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = viewModel.fontSizeScale.sp,
                lineHeight = (viewModel.fontSizeScale * 1.6f).sp
              ),
              textAlign = TextAlign.Center,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }
  }
}
