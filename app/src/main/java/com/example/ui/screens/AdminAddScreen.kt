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
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AppUserItem
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
  val currentUserItem = viewModel.getCurrentUserItem()
  val isOnlyAddUser = currentUserItem.isOnlyAddRole()
  // Tabs: 0: Naats, 1: Categories, 2: Add/Edit Naat, 3: Users & Rights
  var selectedTab by remember { mutableStateOf(if (isOnlyAddUser) 2 else 0) }
  val naats by viewModel.naats.collectAsState()
  val categories = viewModel.categories
  val users = viewModel.users

  val canManageUsers = viewModel.canManageUsers()
  val canManageCategories = viewModel.canManageCategories()
  val isViewOnly = viewModel.isViewOnly()

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

  // Form states for User
  var showUserDialog by remember { mutableStateOf(false) }
  var editingUserId by remember { mutableStateOf<String?>(null) }
  var userName by remember { mutableStateOf("") }
  var userEmail by remember { mutableStateOf("") }
  var userRole by remember { mutableStateOf("ADMIN") } // "ADMIN", "ADD_NAAT", "VIEWER"
  var userAllowedCategories by remember { mutableStateOf<List<String>>(emptyList()) }
  var allowAllCategories by remember { mutableStateOf(true) }
  var deleteUserTarget by remember { mutableStateOf<AppUserItem?>(null) }

  // Permitted categories for the current active user
  val permittedCategories = remember(categories, currentUserItem) {
    if (currentUserItem.isSuperAdmin() || currentUserItem.isAdminRole() || currentUserItem.allowedCategories.isEmpty()) {
      categories
    } else {
      categories.filter { cat -> currentUserItem.allowedCategories.any { it.equals(cat.id, ignoreCase = true) } }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Column {
            Text("Admin Dashboard", fontWeight = FontWeight.Bold)
            Text(
              text = "Role: ${currentUserItem.getRoleDisplayName()}",
              style = MaterialTheme.typography.labelSmall,
              color = Color.White.copy(alpha = 0.8f)
            )
          }
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          if (selectedTab == 0 && !isViewOnly) {
            IconButton(onClick = {
              editingId = null
              title = ""
              reciter = ""
              selectedCategory = permittedCategories.firstOrNull()?.id ?: "naat"
              lyrics = ""
              gujaratiLyrics = ""
              selectedTab = 2
            }) {
              Icon(Icons.Default.Add, contentDescription = "Add New Naat", tint = Color.White)
            }
          } else if (selectedTab == 1 && canManageCategories) {
            IconButton(onClick = {
              editingCategoryId = null
              catTitle = ""
              catSubtitle = ""
              catIcon = "ic_naat"
              showCategoryDialog = true
            }) {
              Icon(Icons.Default.Add, contentDescription = "Add New Category", tint = Color.White)
            }
          } else if (selectedTab == 3 && canManageUsers) {
            IconButton(onClick = {
              editingUserId = null
              userName = ""
              userEmail = ""
              userRole = "ADMIN"
              userAllowedCategories = emptyList()
              allowAllCategories = true
              showUserDialog = true
            }) {
              Icon(Icons.Default.PersonAdd, contentDescription = "Add User", tint = Color.White)
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
      if (selectedTab == 0 && !isViewOnly) {
        FloatingActionButton(
          onClick = {
            editingId = null
            title = ""
            reciter = ""
            selectedCategory = permittedCategories.firstOrNull()?.id ?: "naat"
            lyrics = ""
            gujaratiLyrics = ""
            selectedTab = 2
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Naat")
        }
      } else if (selectedTab == 1 && canManageCategories) {
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
      } else if (selectedTab == 3 && canManageUsers) {
        FloatingActionButton(
          onClick = {
            editingUserId = null
            userName = ""
            userEmail = ""
            userRole = "ADMIN"
            userAllowedCategories = emptyList()
            allowAllCategories = true
            showUserDialog = true
          },
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = Color.White
        ) {
          Icon(Icons.Default.PersonAdd, contentDescription = "Add User")
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
      // Tab selector with ScrollableTabRow for 4 tabs
      ScrollableTabRow(
        selectedTabIndex = selectedTab,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface
      ) {
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
        Tab(
          selected = selectedTab == 3,
          onClick = { selectedTab = 3 },
          text = { Text("Users & Rights (${users.size})", fontWeight = FontWeight.Bold) }
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
                val canEditThis = viewModel.canEditNaat(naat.category)
                val canDeleteThis = viewModel.canDeleteNaat()

                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
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
                      if (canEditThis) {
                        OutlinedButton(
                          onClick = {
                            editingId = naat.id
                            title = naat.title
                            reciter = naat.reciter
                            selectedCategory = naat.category.ifBlank { "naat" }
                            lyrics = naat.hindiLyrics.ifBlank { naat.lyrics }
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
                      }
                      if (canDeleteThis) {
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
        }
        1 -> {
          // Manage Categories List with Reorder Up/Down
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
                    if (canManageCategories) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        // Move Up Button
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
                        // Move Down Button
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
        }
        2 -> {
          // Add / Edit Naat Form
          if (isViewOnly) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
              contentAlignment = Alignment.Center
            ) {
              Text(
                "You have View Only rights. Contact the Super Admin for Add/Edit Naat permissions.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error
              )
            }
          } else {
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

              // Category Dropdown (Respecting permitted categories)
              ExposedDropdownMenuBox(
                expanded = expandedCategory,
                onExpandedChange = { expandedCategory = !expandedCategory }
              ) {
                OutlinedTextField(
                  value = permittedCategories.find { it.id == selectedCategory }?.getDisplayTitle() ?: selectedCategory,
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
                  permittedCategories.forEach { cat ->
                    DropdownMenuItem(
                      text = { Text("${cat.getDisplayTitle()} (${cat.getDisplaySubtitle()})") },
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
        3 -> {
          // Tab 3: Users & Role-Based Rights
          Column(
            modifier = Modifier
              .fillMaxSize()
              .padding(16.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text(
                  text = "App Users & Admin Rights",
                  style = MaterialTheme.typography.titleLarge,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.primary
                )
                Text(
                  text = "Manage Admin, Editor & View-only users",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              if (canManageUsers) {
                Button(
                  onClick = {
                    editingUserId = null
                    userName = ""
                    userEmail = ""
                    userRole = "ADMIN"
                    userAllowedCategories = emptyList()
                    allowAllCategories = true
                    showUserDialog = true
                  },
                  shape = RoundedCornerShape(8.dp)
                ) {
                  Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                  Spacer(modifier = Modifier.width(4.dp))
                  Text("Add User")
                }
              }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
              items(users) { user ->
                val isOwner = user.isSuperAdmin()
                Card(
                  modifier = Modifier.fillMaxWidth(),
                  shape = RoundedCornerShape(12.dp),
                  colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                  elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column(modifier = Modifier.weight(1f)) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                          text = user.name.ifBlank { user.email.substringBefore("@") },
                          style = MaterialTheme.typography.titleMedium,
                          fontWeight = FontWeight.Bold,
                          color = MaterialTheme.colorScheme.onSurface
                        )
                        if (isOwner) {
                          Spacer(modifier = Modifier.width(6.dp))
                          Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primaryContainer
                          ) {
                            Text(
                              text = "OWNER",
                              style = MaterialTheme.typography.labelSmall,
                              color = MaterialTheme.colorScheme.primary,
                              fontWeight = FontWeight.Bold,
                              modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                          }
                        }
                      }

                      Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                      )

                      Spacer(modifier = Modifier.height(6.dp))

                      // Role Tag
                      Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (user.role.uppercase()) {
                          "ADMIN" -> Color(0xFFE0F2FE)
                          "ONLY_ADD_NAAT" -> Color(0xFFFEF3C7)
                          "ADD_NAAT" -> Color(0xFFDCFCE7)
                          else -> Color(0xFFF3F4F6)
                        }
                      ) {
                        Text(
                          text = when (user.role.uppercase()) {
                            "ADMIN" -> "👑 Full Admin (All Rights)"
                            "ONLY_ADD_NAAT" -> if (user.allowedCategories.isEmpty()) "➕ Only Add Naat (No Edit/Delete)" else "➕ Only Add Naat (${user.allowedCategories.size} Categories)"
                            "ADD_NAAT" -> if (user.allowedCategories.isEmpty()) "📝 Editor (Add & Edit)" else "📝 Editor (${user.allowedCategories.size} Categories)"
                            else -> "👁️ Viewer (View Only)"
                          },
                          style = MaterialTheme.typography.labelSmall,
                          fontWeight = FontWeight.Bold,
                          color = when (user.role.uppercase()) {
                            "ADMIN" -> Color(0xFF0369A1)
                            "ONLY_ADD_NAAT" -> Color(0xFFB45309)
                            "ADD_NAAT" -> Color(0xFF15803D)
                            else -> Color(0xFF4B5563)
                          },
                          modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                      }
                    }

                    if (canManageUsers && !isOwner) {
                      Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = {
                          editingUserId = user.id
                          userName = user.name
                          userEmail = user.email
                          userRole = user.role
                          userAllowedCategories = user.allowedCategories
                          allowAllCategories = user.allowedCategories.isEmpty()
                          showUserDialog = true
                        }) {
                          Icon(Icons.Default.Edit, contentDescription = "Edit User", tint = MaterialTheme.colorScheme.primary)
                        }
                        IconButton(onClick = { deleteUserTarget = user }) {
                          Icon(Icons.Default.Delete, contentDescription = "Delete User", tint = MaterialTheme.colorScheme.error)
                        }
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }

  // Add / Edit User Dialog
  if (showUserDialog) {
    AlertDialog(
      onDismissRequest = { showUserDialog = false },
      title = {
        Text(
          text = if (editingUserId == null) "Add User & Assign Rights" else "Edit User Rights",
          fontWeight = FontWeight.Bold
        )
      },
      text = {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState()),
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          OutlinedTextField(
            value = userName,
            onValueChange = { userName = it },
            label = { Text("User Name (e.g. Ahmed Raza)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp)
          )

          OutlinedTextField(
            value = userEmail,
            onValueChange = { userEmail = it },
            label = { Text("User Email (Login ID)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = RoundedCornerShape(8.dp),
            enabled = editingUserId == null
          )

          Text(
            text = "Select User Role & Permissions:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
          )

          // Role 1: Full Admin
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { userRole = "ADMIN" },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (userRole == "ADMIN") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = userRole == "ADMIN",
                onClick = { userRole = "ADMIN" }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("👑 Full Admin Rights", fontWeight = FontWeight.Bold)
                Text("Can manage users, categories, reorder, all Naats", style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          // Role 2: Only Add Naat Sharif (NO Edit, NO Delete)
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { userRole = "ONLY_ADD_NAAT" },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (userRole == "ONLY_ADD_NAAT") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = userRole == "ONLY_ADD_NAAT",
                onClick = { userRole = "ONLY_ADD_NAAT" }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("➕ Only Add Naat Sharif (No Edit/Delete)", fontWeight = FontWeight.Bold)
                Text("Can ONLY add new Naats. Strictly NO edit, modify, or delete option allowed.", style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          // Role 3: Editor (Add & Edit Naats)
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { userRole = "ADD_NAAT" },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (userRole == "ADD_NAAT") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = userRole == "ADD_NAAT",
                onClick = { userRole = "ADD_NAAT" }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("📝 Editor (Add & Edit Naats)", fontWeight = FontWeight.Bold)
                Text("Can add & edit Naats in permitted categories", style = MaterialTheme.typography.bodySmall)
              }
            }
          }

          // If Only Add Naat or Editor selected, show Category checklist
          if (userRole == "ONLY_ADD_NAAT" || userRole == "ADD_NAAT") {
            Card(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(8.dp),
              colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Text(
                  text = "Allowed Categories for Adding Naat:",
                  style = MaterialTheme.typography.labelMedium,
                  fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                  verticalAlignment = Alignment.CenterVertically,
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                      allowAllCategories = !allowAllCategories
                      if (allowAllCategories) {
                        userAllowedCategories = emptyList()
                      }
                    }
                ) {
                  Checkbox(
                    checked = allowAllCategories,
                    onCheckedChange = {
                      allowAllCategories = it
                      if (it) userAllowedCategories = emptyList()
                    }
                  )
                  Text("All Categories", fontWeight = FontWeight.Medium)
                }

                if (!allowAllCategories) {
                  categories.forEach { cat ->
                    val isChecked = userAllowedCategories.contains(cat.id)
                    Row(
                      verticalAlignment = Alignment.CenterVertically,
                      modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                          userAllowedCategories = if (isChecked) {
                            userAllowedCategories - cat.id
                          } else {
                            userAllowedCategories + cat.id
                          }
                        }
                    ) {
                      Checkbox(
                        checked = isChecked,
                        onCheckedChange = { checked ->
                          userAllowedCategories = if (checked) {
                            userAllowedCategories + cat.id
                          } else {
                            userAllowedCategories - cat.id
                          }
                        }
                      )
                      Text("${cat.getDisplayTitle()} (${cat.getDisplaySubtitle()})")
                    }
                  }
                }
              }
            }
          }

          // Role 4: Only View
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable { userRole = "VIEWER" },
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(
              containerColor = if (userRole == "VIEWER") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            )
          ) {
            Row(
              modifier = Modifier.padding(12.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              RadioButton(
                selected = userRole == "VIEWER",
                onClick = { userRole = "VIEWER" }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text("👁️ Only View Rights", fontWeight = FontWeight.Bold)
                Text("Read-only access, cannot add/edit/delete content", style = MaterialTheme.typography.bodySmall)
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (userEmail.isBlank()) return@Button
            val finalAllowedCategories = if (allowAllCategories || (userRole != "ADD_NAAT" && userRole != "ONLY_ADD_NAAT")) emptyList() else userAllowedCategories
            if (editingUserId == null) {
              viewModel.addUser(userName, userEmail, userRole, finalAllowedCategories) {
                showUserDialog = false
              }
            } else {
              viewModel.updateUser(editingUserId!!, userName, userEmail, userRole, finalAllowedCategories) {
                showUserDialog = false
              }
            }
          }
        ) {
          Text(if (editingUserId == null) "Add User" else "Save Rights")
        }
      },
      dismissButton = {
        TextButton(onClick = { showUserDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete User Confirmation Dialog
  if (deleteUserTarget != null) {
    AlertDialog(
      onDismissRequest = { deleteUserTarget = null },
      title = { Text("Delete User") },
      text = { Text("Are you sure you want to remove ${deleteUserTarget?.name ?: deleteUserTarget?.email} from admin users?") },
      confirmButton = {
        Button(
          onClick = {
            deleteUserTarget?.let {
              viewModel.deleteUser(it.id) {
                deleteUserTarget = null
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete User")
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteUserTarget = null }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Naat Confirmation Dialog
  if (deleteTarget != null) {
    AlertDialog(
      onDismissRequest = { deleteTarget = null },
      title = { Text("Delete Naat Sharif") },
      text = { Text("Are you sure you want to delete \"${deleteTarget?.title}\"? This action cannot be undone.") },
      confirmButton = {
        Button(
          onClick = {
            deleteTarget?.let {
              viewModel.deleteNaat(it.id) {
                deleteTarget = null
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteTarget = null }) {
          Text("Cancel")
        }
      }
    )
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
            label = { Text("Title (e.g. हम्द शरीफ)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
          OutlinedTextField(
            value = catSubtitle,
            onValueChange = { catSubtitle = it },
            label = { Text("Subtitle (e.g. Hamd Sharif)") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            if (catTitle.isBlank()) return@Button
            if (editingCategoryId == null) {
              viewModel.addCategory(CategoryItem(title = catTitle, subtitle = catSubtitle, iconName = catIcon)) {
                showCategoryDialog = false
              }
            } else {
              viewModel.updateCategory(CategoryItem(id = editingCategoryId!!, title = catTitle, subtitle = catSubtitle, iconName = catIcon)) {
                showCategoryDialog = false
              }
            }
          }
        ) {
          Text(if (editingCategoryId == null) "Add Category" else "Save")
        }
      },
      dismissButton = {
        TextButton(onClick = { showCategoryDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // Delete Category Confirmation Dialog
  if (deleteCategoryTarget != null) {
    AlertDialog(
      onDismissRequest = { deleteCategoryTarget = null },
      title = { Text("Delete Category") },
      text = { Text("Are you sure you want to delete category \"${deleteCategoryTarget?.getDisplayTitle()}\"?") },
      confirmButton = {
        Button(
          onClick = {
            deleteCategoryTarget?.let {
              viewModel.deleteCategory(it.id) {
                deleteCategoryTarget = null
              }
            }
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Delete")
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteCategoryTarget = null }) {
          Text("Cancel")
        }
      }
    )
  }
}
