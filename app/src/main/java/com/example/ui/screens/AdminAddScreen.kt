package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.viewmodel.NaatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddScreen(
  viewModel: NaatViewModel,
  onAdded: () -> Unit,
  onBack: () -> Unit
) {
  var title by remember { mutableStateOf("") }
  var reciter by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("naat") }
  var lyrics by remember { mutableStateOf("") }
  var gujaratiLyrics by remember { mutableStateOf("") }
  var expandedCategory by remember { mutableStateOf(false) }
  var isSubmitting by remember { mutableStateOf(false) }
  val scrollState = rememberScrollState()

  val categoryOptions = viewModel.categories

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Add Naat / Kalam (Admin)", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = Color.White,
          navigationIconContentColor = Color.White
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .verticalScroll(scrollState)
        .padding(20.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      OutlinedTextField(
        value = title,
        onValueChange = { title = it },
        label = { Text("Naat Title (e.g. 10. नई नात शरीफ)") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      OutlinedTextField(
        value = reciter,
        onValueChange = { reciter = it },
        label = { Text("Reciter / Poet (e.g. Owais Raza Qadri)") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      ExposedDropdownMenuBox(
        expanded = expandedCategory,
        onExpandedChange = { expandedCategory = !expandedCategory }
      ) {
        OutlinedTextField(
          value = categoryOptions.find { it.id == selectedCategory }?.title ?: selectedCategory,
          onValueChange = {},
          readOnly = true,
          label = { Text("Category") },
          trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
          modifier = Modifier
            .fillMaxWidth()
            .menuAnchor(),
          shape = RoundedCornerShape(12.dp)
        )
        ExposedDropdownMenu(
          expanded = expandedCategory,
          onDismissRequest = { expandedCategory = false }
        ) {
          categoryOptions.forEach { cat ->
            DropdownMenuItem(
              text = { Text(cat.title) },
              onClick = {
                selectedCategory = cat.id
                expandedCategory = false
              }
            )
          }
        }
      }

      OutlinedTextField(
        value = lyrics,
        onValueChange = { lyrics = it },
        label = { Text("Hindi Lyrics (हिन्दी)") },
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp),
        shape = RoundedCornerShape(12.dp)
      )

      OutlinedTextField(
        value = gujaratiLyrics,
        onValueChange = { gujaratiLyrics = it },
        label = { Text("Gujarati Lyrics (ગુજરાતી)") },
        modifier = Modifier
          .fillMaxWidth()
          .height(150.dp),
        shape = RoundedCornerShape(12.dp)
      )

      Button(
        onClick = {
          if (title.isNotBlank() && lyrics.isNotBlank()) {
            isSubmitting = true
            // we can pass gujaratiLyrics by adding it or in NaatItem
            viewModel.addNaat(
              title = title,
              category = selectedCategory,
              reciter = reciter.ifBlank { "Traditional" },
              lyrics = lyrics,
              gujaratiLyrics = gujaratiLyrics
            ) { success ->
              isSubmitting = false
              if (success) {
                onAdded()
              }
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isSubmitting) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
          Text("Publish Naat / Kalam Sharif", fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}
