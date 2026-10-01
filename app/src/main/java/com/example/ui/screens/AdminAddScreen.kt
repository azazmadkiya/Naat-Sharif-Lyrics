package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
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
import com.example.model.CategoryItem
import com.example.model.NaatItem
import com.example.viewmodel.NaatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminAddScreen(
  viewModel: NaatViewModel,
  onAdded: () -> Unit,
  onBack: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0: Manage Naats, 1: Manage Categories, 2: Add/Edit Naat
  val naats by viewModel.naats.collectAsState()
  val categories = viewModel.categories

  // Form states for Naat
  var editingId by remember { mutableStateOf<String?>(null) }
  var title by remember { mutableStateOf("") }
  var reciter by remember { mutableStateOf("") }
  var selectedCategory by remember { mutableStateOf("naat") }
  var lyrics by remember { mutableStateOf("") }
  var gujaratiLyrics by remember { mutableStateOf("") }
  var expandedCategory by remember { mutableStateOf(false) }
  var isSubmitting by remember { mutableStateOf(false) }
  var deleteTarget by remember { mutableStateOf<NaatItem?>(null) }

  // Form states for Category
  var showCategoryDialog by remember { mutableStateOf(false) }
  var editingCategoryId by remember { mutableStateOf<String?>(null) }
  var catTitle by remember { mutableStateOf("") }
  var catSubtitle by remember { mutableStateOf("") }
  var catIcon by remember { mutableStateOf("ic_naat") }
  var deleteCategoryTarget by remember { mutableStateOf<CategoryItem?>(null) }

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
          if (selectedTab == 0) {
            IconButton(onClick = {
              editingId = null
              title = ""
              reciter = ""
              selectedCategory = categories.firstOrNull()?.id ?: "naat"
              lyrics = ""
              gujaratiLyrics = ""
              selectedTab = 2
            }) {
              Icon(Icons.Default.Add, contentDescription = "Add New Naat", tint = Color.White)
            }
          } else if (selectedTab == 1) {
            IconButton(onClick = {
              editingCategoryId = null
              catTitle = ""
              catSubtitle = ""
              catIcon = "ic_naat"
              showCategoryDialog = true
            }) {
              Icon(Icons.Default.Add, contentDescription = "Add New Category", tint = Color.White)
            }
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
            selectedCategory = categories.firstOrNull()?.id ?: "naat"
            lyrics = ""
            gujaratiLyrics = ""
            selectedTab = 2
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Naat")
        }
      } else if (selectedTab == 1) {
        FloatingActionButton(
          onClick = {
            editingCategoryId = null
            catTitle = ""
            catSubtitle = ""
            catIcon = "ic_naat"
            showCategoryDialog = true
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Category")
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
          text = { Text("Naats (${naats.size})", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 1,
          onClick = { selectedTab = 1 },
          text = { Text("Categories (${categories.size})", fontWeight = FontWeight.Bold) }
        )
        Tab(
          selected = selectedTab == 2,
          onClick = { selectedTab = 2 },
          text = { Text(if (editingId == null) "Add Naat" else "Edit Naat", fontWeight = FontWeight.Bold) }
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
                      text = "Category: ${naat.category} | Reciter: ${naat.reciter}",
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
                          lyrics = naat.lyrics
                          gujaratiLyrics = naat.gujaratiLyrics
                          selectedTab = 2
                        },
                        modifier = Modifier.height(36.dp),
                        shape = RoundedCornerShape(8.dp)
                      ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit")
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
          // Manage Categories List
          if (categories.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
              Text("No categories found.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          } else {
            LazyColumn(
              modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              itemsIndexed(categories) { index, cat ->
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = "${index + 1}. ${cat.getDisplayTitle()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                      )
                      Spacer(modifier = Modifier.height(2.dp))
                      Text(
                        text = "${cat.getDisplaySubtitle()} (ID: ${cat.id})",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                      // Move Up Button (Uper karein)
                      IconButton(
                        onClick = { viewModel.moveCategoryUp(index) },
                        enabled = index > 0
                      ) {
                        Icon(
                          imageVector = Icons.Default.ArrowUpward,
                          contentDescription = "Move Up",
                          tint = if (index > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                      }
                      // Move Down Button (Niche karein)
                      IconButton(
                        onClick = { viewModel.moveCategoryDown(index) },
                        enabled = index < categories.size - 1
                      ) {
                        Icon(
                          imageVector = Icons.Default.ArrowDownward,
                          contentDescription = "Move Down",
                          tint = if (index < categories.size - 1) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                      }
                      // Edit Button
                      IconButton(onClick = {
                        editingCategoryId = cat.id
                        catTitle = cat.title
                        catSubtitle = cat.subtitle
                        catIcon = cat.iconName.ifBlank { "ic_naat" }
                        showCategoryDialog = true
                      }) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Category", tint = MaterialTheme.colorScheme.secondary)
                      }
                      // Delete Button
                      IconButton(onClick = { deleteCategoryTarget = cat }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete Category", tint = MaterialTheme.colorScheme.error)
                      }
                    }
                  }
                }
              }
            }
          }
        }
        2 -> {
          // Add / Edit Naat Form
          Column(
            modifier = Modifier
              .fillMaxSize()
              .verticalScroll(rememberScrollState())
              .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = if (editingId == null) "Add New Naat Sharif" else "Edit Naat Sharif",
              style = MaterialTheme.typography.titleLarge,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.primary
            )

            OutlinedTextField(
              value = title,
              onValueChange = { title = it },
              label = { Text("Title (e.g. अक्से रूए मुस्तफा)") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              singleLine = true
            )

            // Category Dropdown
            ExposedDropdownMenuBox(
              expanded = expandedCategory,
              onExpandedChange = { expandedCategory = !expandedCategory }
            ) {
              OutlinedTextField(
                value = categories.find { it.id == selectedCategory }?.title ?: selectedCategory,
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
                categories.forEach { cat ->
                  DropdownMenuItem(
                    text = { Text("${cat.title} (${cat.subtitle})") },
                    onClick = {
                      selectedCategory = cat.id
                      expandedCategory = false
                    }
                  )
                }
              }
            }

            OutlinedTextField(
              value = reciter,
              onValueChange = { reciter = it },
              label = { Text("Reciter / Poet (Optional)") },
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(12.dp),
              singleLine = true
            )

            OutlinedTextField(
              value = lyrics,
              onValueChange = { lyrics = it },
              label = { Text("Hindi / Urdu Lyrics") },
              modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
              shape = RoundedCornerShape(12.dp)
            )

            OutlinedTextField(
              value = gujaratiLyrics,
              onValueChange = { gujaratiLyrics = it },
              label = { Text("Gujarati / English Transliteration (Optional)") },
              modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
              shape = RoundedCornerShape(12.dp)
            )

            Button(
              onClick = {
                if (title.isBlank() || lyrics.isBlank()) {
                  return@Button
                }
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
                      selectedTab = 0
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

  // Add / Edit Category Dialog
  if (showCategoryDialog) {
    AlertDialog(
      onDismissRequest = { showCategoryDialog = false },
      title = { Text(if (editingCategoryId == null) "Add New Category" else "Edit Category") },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedTextField(
            value = catTitle,
            onValueChange = { catTitle = it },
            label = { Text("Category Title (e.g. हम्द शरीफ)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = catSubtitle,
            onValueChange = { catSubtitle = it },
            label = { Text("Subtitle (e.g. Hamd Sharif)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
          OutlinedTextField(
            value = catIcon,
            onValueChange = { catIcon = it },
            label = { Text("Icon Name (e.g. ic_hamd)") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (catTitle.isNotBlank()) {
              val id = editingCategoryId ?: catTitle.lowercase().replace(" ", "_")
              val item = CategoryItem(
                id = id,
                title = catTitle,
                subtitle = catSubtitle.ifBlank { catTitle },
                iconName = catIcon.ifBlank { "ic_naat" },
                count = 0
              )
              if (editingCategoryId == null) {
                viewModel.addCategory(item) { showCategoryDialog = false }
              } else {
                viewModel.updateCategory(item) { showCategoryDialog = false }
              }
            }
          }
        ) {
          Text(if (editingCategoryId == null) "Add" else "Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCategoryDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Naat Confirmation Dialog
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

  // Delete Category Confirmation Dialog
  if (deleteCategoryTarget != null) {
    AlertDialog(
      onDismissRequest = { deleteCategoryTarget = null },
      title = { Text("Confirm Delete Category") },
      text = { Text("Are you sure you want to delete category '${deleteCategoryTarget?.title}'?") },
      confirmButton = {
        Button(
          onClick = {
            val target = deleteCategoryTarget
            deleteCategoryTarget = null
            if (target != null) {
              viewModel.deleteCategory(target.id) { success ->
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
        OutlinedButton(onClick = { deleteCategoryTarget = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
