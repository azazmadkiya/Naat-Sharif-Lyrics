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
      // Admin Section if logged in as Admin
      if (viewModel.isAdmin) {
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
                  text = "Add new Naat Sharif & Kalam Sharif",
                  style = MaterialTheme.typography.bodySmall,
                  color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
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
            putExtra(Intent.EXTRA_TEXT, "Check out Naat Sharif Hindi Lyrics App: https://play.google.com/store/apps/details?id=com.aistudio.naatsharif")
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
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.aistudio.naatsharif")))
          } catch (e: Exception) {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.aistudio.naatsharif")))
          }
        }
      }

      // Privacy Policy
      item {
        SettingItem(
          icon = Icons.Default.PrivacyTip,
          title = "Privacy Policy",
          subtitle = "Read our privacy policy"
        ) {
          context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com")))
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
