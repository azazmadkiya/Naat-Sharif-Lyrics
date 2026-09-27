package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CategoryItem
import com.example.ui.theme.IslamicGreen
import com.example.ui.theme.IslamicGreenDark
import com.example.ui.theme.IslamicGreenLight
import com.example.ui.theme.GoldAccent
import com.example.viewmodel.NaatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
  viewModel: NaatViewModel,
  onCategoryClick: (String, String) -> Unit,
  onNavigate: (String) -> Unit
) {
  var showDonateDialog by remember { mutableStateOf(false) }

  Scaffold(
    bottomBar = {
      NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
      ) {
        NavigationBarItem(
          icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
          label = { Text("Home") },
          selected = true,
          onClick = { }
        )
        NavigationBarItem(
          icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
          label = { Text("Search") },
          selected = false,
          onClick = { onNavigate("search") }
        )
        NavigationBarItem(
          icon = { Icon(Icons.Default.Favorite, contentDescription = "Favorites") },
          label = { Text("Favorites") },
          selected = false,
          onClick = { onNavigate("favorites") }
        )
        NavigationBarItem(
          icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
          label = { Text("Setting") },
          selected = false,
          onClick = { onNavigate("settings") }
        )
      }
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Top Green Header matching Screenshot #1
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .background(
            brush = Brush.verticalGradient(
              colors = listOf(IslamicGreenLight, IslamicGreenDark)
            )
          )
          .padding(24.dp),
        contentAlignment = Alignment.Center
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          // App Logo badge simulation
          Surface(
            shape = RoundedCornerShape(50),
            color = Color(0xFF111827),
            modifier = Modifier.size(56.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Text("नात", color = GoldAccent, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
          }
          Spacer(modifier = Modifier.height(12.dp))
          Text(
            text = "नातों का ख़ज़ाना",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = Color.White
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "03 Jun 2026  |  17 Zil-Hijjah 1447 AH",
            style = MaterialTheme.typography.bodyMedium,
            color = Color(0xFFE5E7EB)
          )
          Spacer(modifier = Modifier.height(16.dp))

          Button(
            onClick = { showDonateDialog = true },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            shape = RoundedCornerShape(20.dp)
          ) {
            Text("डोनेशन करे", color = IslamicGreenDark, fontWeight = FontWeight.Bold)
          }
        }
      }

      // Categories Grid matching Screenshot #1
      LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        items(viewModel.categories) { cat ->
          CategoryCard(category = cat) {
            onCategoryClick(cat.id, cat.title)
          }
        }
      }
    }
  }

  if (showDonateDialog) {
    AlertDialog(
      onDismissRequest = { showDonateDialog = false },
      title = { Text("Support Naat Sharif App") },
      text = { Text("Jazakallah Khair for your support! You can support via UPI or Bank Transfer to keep this app running ad-free for lovers of Naat.") },
      confirmButton = {
        Button(onClick = { showDonateDialog = false }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
fun CategoryCard(category: CategoryItem, onClick: () -> Unit) {
  Card(
    modifier = Modifier
      .fillMaxWidth()
      .height(100.dp)
      .clickable(onClick = onClick),
    shape = RoundedCornerShape(16.dp),
    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
  ) {
    Row(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center
    ) {
      Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
          text = category.title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = category.subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant,
          textAlign = TextAlign.Center
        )
      }
    }
  }
}
