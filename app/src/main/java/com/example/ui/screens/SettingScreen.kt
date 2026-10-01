package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import com.example.viewmodel.NaatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen(
  viewModel: NaatViewModel,
  onNavigateAdmin: () -> Unit,
  onNavigatePrivacy: () -> Unit,
  onNavigateLogin: () -> Unit,
  onLogout: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current
  var showLogoutDialog by remember { mutableStateOf(false) }
  val currentUserItem = viewModel.getCurrentUserItem()
  val isUserLoggedIn = viewModel.isLoggedIn || viewModel.isAdmin

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Settings", fontWeight = FontWeight.Bold) },
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
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Top Account Status Header
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(
            containerColor = if (isUserLoggedIn) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
          ),
          elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Surface(
              shape = RoundedCornerShape(50),
              color = if (isUserLoggedIn) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
              modifier = Modifier.size(48.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = if (isUserLoggedIn) Icons.Default.Person else Icons.Default.AccountCircle,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(28.dp)
                )
              }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
              if (isUserLoggedIn) {
                Text(
                  text = viewModel.loggedInEmail ?: "Admin User",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(modifier = Modifier.height(2.dp))
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = MaterialTheme.colorScheme.primary
                ) {
                  Text(
                    text = currentUserItem.getRoleDisplayName(),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                  )
                }
              } else {
                Text(
                  text = "Guest User",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = "Login to access Admin rights or add Naats",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
              }
            }

            if (isUserLoggedIn) {
              IconButton(onClick = { showLogoutDialog = true }) {
                Icon(
                  Icons.Default.Logout,
                  contentDescription = "Logout",
                  tint = MaterialTheme.colorScheme.error
                )
              }
            } else {
              Button(
                onClick = onNavigateLogin,
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Login")
              }
            }
          }
        }
      }

      // Admin Section if logged in as Admin / Editor
      if (viewModel.isAdmin || currentUserItem.isAdminRole() || currentUserItem.isAddNaatRole()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = onNavigateAdmin),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(Icons.Default.AdminPanelSettings, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
              Spacer(modifier = Modifier.width(16.dp))
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Admin Dashboard",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = "Role: ${currentUserItem.getRoleDisplayName()} • Manage Naats, Categories & Rights",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }

      // Dark Mode Toggle
      item {
        Card(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(
              imageVector = if (viewModel.isDarkMode) Icons.Default.DarkMode else Icons.Default.LightMode,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
              text = "Night Reading Mode",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.weight(1f),
              color = MaterialTheme.colorScheme.onSurface
            )
            Switch(
              checked = viewModel.isDarkMode,
              onCheckedChange = { viewModel.isDarkMode = it }
            )
          }
        }
      }

      // Share App
      item {
        SettingItem(
          icon = Icons.Default.Share,
          title = "Share App",
          subtitle = "Share with friends & family"
        ) {
          val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, "Check out Naat Sharif & Kalam Sharif Hindi/Gujarati App! Download now from Google Play Store: https://play.google.com/store/apps/details?id=com.NaatSharif.Lyrics.azaz")
            type = "text/plain"
          }
          context.startActivity(Intent.createChooser(shareIntent, null))
        }
      }

      // Rate Us
      item {
        SettingItem(
          icon = Icons.Default.Star,
          title = "Rate Us",
          subtitle = "Give 5 stars on Play Store"
        ) {
          try {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.NaatSharif.Lyrics.azaz")))
          } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.NaatSharif.Lyrics.azaz")))
          }
        }
      }

      // Privacy Policy
      item {
        SettingItem(
          icon = Icons.Default.PrivacyTip,
          title = "Privacy Policy & Terms",
          subtitle = "Read our privacy policy and terms"
        ) {
          onNavigatePrivacy()
        }
      }

      // Explicit Login / Logout Row
      if (isUserLoggedIn) {
        item {
          SettingItem(
            icon = Icons.Default.Logout,
            title = "Logout",
            subtitle = "Sign out from ${viewModel.loggedInEmail ?: "your account"}"
          ) {
            showLogoutDialog = true
          }
        }
      } else {
        item {
          SettingItem(
            icon = Icons.Default.Login,
            title = "Login / Sign In",
            subtitle = "Sign in with your email address for Admin or Editor access"
          ) {
            onNavigateLogin()
          }
        }
      }

      // Footer Credit: Developed By Azazmadkiya
      item {
        Spacer(modifier = Modifier.height(16.dp))
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .clickable {
              try {
                val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                  data = Uri.parse("mailto:azazmadkiya@gmail.com")
                  putExtra(Intent.EXTRA_SUBJECT, "Naat Sharif App Inquiry")
                }
                context.startActivity(emailIntent)
              } catch (_: Exception) {}
            },
          shape = RoundedCornerShape(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Surface(
              shape = RoundedCornerShape(50),
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.size(44.dp)
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Code,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(24.dp)
                )
              }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "Developed By",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Azazmadkiya",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
              )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "azazmadkiya@gmail.com",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.padding(top = 2.dp)
            ) {
              Text(
                text = "Naat Sharif & Kalam Sharif App v1.0",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }
        Spacer(modifier = Modifier.height(16.dp))
      }
    }
  }

  // Logout Dialog
  if (showLogoutDialog) {
    AlertDialog(
      onDismissRequest = { showLogoutDialog = false },
      title = { Text("Confirm Logout") },
      text = { Text("Are you sure you want to log out of your account?") },
      confirmButton = {
        Button(
          onClick = {
            showLogoutDialog = false
            viewModel.signOut()
            Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()
            onLogout()
          },
          colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
          Text("Logout")
        }
      },
      dismissButton = {
        TextButton(onClick = { showLogoutDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun SettingItem(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(12.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
      Spacer(modifier = Modifier.width(16.dp))
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
      Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
  }
}
