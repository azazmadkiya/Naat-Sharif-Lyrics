package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
  onLogout: () -> Unit,
  onBack: () -> Unit
) {
  val context = LocalContext.current

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
      // Admin Section if logged in as Admin / Editor / Content Manager
      val currentUserItem = viewModel.getCurrentUserItem()
      if (viewModel.isAdmin || currentUserItem.isAdminRole() || currentUserItem.isAddNaatRole()) {
        item {
          Card(
            modifier = Modifier
              .fillMaxWidth()
              .clickable(onClick = onNavigateAdmin),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
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
                  color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                  text = "Role: ${currentUserItem.getRoleDisplayName()} • Manage Naats, Categories & Rights",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
              }
              Icon(Icons.Default.ChevronRight, contentDescription = null)
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



      // Logout
      if (viewModel.currentUser != null) {
        item {
          SettingItem(
            icon = Icons.Default.Logout,
            title = "Logout",
            subtitle = "Sign out of your account"
          ) {
            viewModel.signOut()
            onLogout()
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
