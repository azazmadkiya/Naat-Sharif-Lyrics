package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.NaatItem
import com.example.viewmodel.NaatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddScreen(
  viewModel: NaatViewModel,
  onAdded: () -> Unit,
  onBack: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Manage Naats, 1: Add/Edit Naat
  val naats by viewModel.naats.collectAsState()

  // Form states
  var editingId by remember { mutableStateOf<String?>(null) }
  var title by remember { mutableStateOf("") }
  var reciter by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("naat") }
  var lyrics by remember { mutableStateOf("") }
  var gujaratiLyrics by remember { mutableStateOf("") }
  var expandedCategory by remember { mutableStateOf(false) }
  var isSubmitting by remember { mutableStateOf(false) }
  var deleteTarget by remember { mutableStateOf<NaatItem?>(null) }

  val categoryOptions = viewModel.categories

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Admin Dashboard (Full Access)", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = {
            editingId = null
            title = ""
            reciter = ""
            selectedCategory = "naat"
            lyrics = ""
            gujaratiLyrics = ""
            selectedTab = 1
          }) {
            Icon(Icons.Default.Add, contentDescription = "Add New Naat", tint = Color.White)
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
    floatingActionButton = {
      if (selectedTab == 0) {
        FloatingActionButton(
          onClick = {
            editingId = null
            title = ""
            reciter = ""
            selectedCategory = "naat"
            lyrics = ""
            gujaratiLyrics = ""
            selectedTab = 1
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Naat")
        }
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Tab selector
      TabRow(selectedTabIndex = selectedTab) {
        Tab(
          selected = selectedTab == 0,
          onClick = { selectedTab = 0 },
          text = { Text("Manage Naats (${naats.size})", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text(if (editingId == null) "Add New Naat" else "Edit Naat", fontWeight = FontWeight.Bold) }
        )
      }

      when (selectedTab) {
        0 -> {
          // Manage / Edit / Delete Naats List
          if (naats.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("No Naat Sharif found in database.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(naats) { naat ->
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                  Column(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(16.dp)
                  ) {
                    Text(
                      text = naat.title,
                      style = MaterialTheme.typography.titleMedium,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Reciter: ${naat.reciter.ifBlank { "Traditional" }} | Category: ${naat.category.uppercase()}",
                      style = MaterialTheme.typography.bodySmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.End,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      OutlinedButton(
                        onClick = {
                          editingId = naat.id
                          title = naat.title
                          reciter = naat.reciter
                          selectedCategory = naat.category.ifBlank { "naat" }
                          lyrics = naat.lyrics.ifBlank { naat.hindiLyrics }
                          gujaratiLyrics = naat.gujaratiLyrics
                          selectedTab = 1
                        },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                      ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Modify / Edit")
                      }
                      Spacer(modifier = Modifier.width(8.dp))
                      Button(
                        onClick = { deleteTarget = naat },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                      ) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete")
                      }
                    }
                  }
                }
              }
            }
          }
        }
        1 -> {
          // Add or Edit Form
          val scrollState = rememberScrollState()
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(scrollState)
              .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = if (editingId == null) "Add New Naat / Kalam Sharif" else "Modify / Edit Naat Sharif",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
              value = title,
              onValueChange = { title = it },
              label = { Text("Naat Title (e.g. नई नात शरीफ)") },
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
                  if (editingId == null) {
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
                  } else {
                    viewModel.updateNaat(
                      id = editingId!!,
                      title = title,
                      category = selectedCategory,
                      reciter = reciter.ifBlank { "Traditional" },
                      lyrics = lyrics,
                      gujaratiLyrics = gujaratiLyrics
                    ) { success ->
                      isSubmitting = false
                      if (success) {
                        selectedTab = 0
                      }
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
                Text(
                  text = if (editingId == null) "Publish Naat Sharif" else "Save Changes",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp
                )
              }
            }
          }
        }
      }
    }
  }

  // Delete Confirmation Dialog
  if (deleteTarget != null) {
    AlertDialog(
      onDismissRequest = { deleteTarget = null },
      title = { Text("Confirm Delete") },
      text = { Text("Are you sure you want to delete '${deleteTarget?.title}'? This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            val target = deleteTarget
            deleteTarget = null
            if (target != null) {
              viewModel.deleteNaat(target.id) { success ->
                // deleted
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        OutlinedButton(onClick = { deleteTarget = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
