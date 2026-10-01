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
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
          .padding(24.dp)
      ) {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Box(modifier = Modifier.fillMaxWidth()) {
            Column(
              modifier = Modifier.align(Alignment.Center),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                text = "Naat Sharif / Kalam Sharif Hindi - Gujarati",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
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

            IconButton(
              onClick = { viewModel.refreshData() },
              modifier = Modifier.align(Alignment.TopEnd)
            ) {
              if (viewModel.isRefreshing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
              } else {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh App", tint = Color.White)
              }
            }
          }
        }
      }

      // No Internet Connection Error Banner with Refresh Button
      if (!viewModel.isConnected || viewModel.errorMessage != null) {
        Card(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
          shape = RoundedCornerShape(12.dp)
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = "No internet connection",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = viewModel.errorMessage ?: "Please check your network and try again.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onErrorContainer
              )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
              onClick = { viewModel.refreshData() },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
              if (viewModel.isRefreshing) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
              } else {
                Text("Refresh")
              }
            }
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
            onCategoryClick(cat.id, cat.getDisplayTitle())
          }
        }
      }
    }
  }

  if (showDonateDialog) {
    val context = LocalContext.current
    AlertDialog(
      onDismissRequest = { showDonateDialog = false },
      title = { 
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red)
          Spacer(modifier = Modifier.width(8.dp))
          Text("Support & Donate")
        }
      },
      text = {
        Column(
          modifier = Modifier.fillMaxWidth(),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          Text(
            "Jazakallah Khair for supporting Naat Sharif App! Contributions help keep this app ad-free and maintained.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
          )


          // UPI ID box
          Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant
          ) {
            Row(
              modifier = Modifier.fillMaxWidth().padding(12.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column {
                Text("UPI ID", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("azazmadkiya@oksbi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
              }
              TextButton(onClick = {
                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("UPI ID", "azazmadkiya@oksbi")
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "UPI ID copied to clipboard!", Toast.LENGTH_SHORT).show()
              }) {
                Text("Copy")
              }
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            try {
              val uri = Uri.parse("upi://pay?pa=azazmadkiya@oksbi&pn=Azazmadkiya&cu=INR")
              val intent = Intent(Intent.ACTION_VIEW, uri)
              context.startActivity(intent)
            } catch (e: Exception) {
              Toast.makeText(context, "No UPI app found on device", Toast.LENGTH_SHORT).show()
            }
          },
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Pay via UPI App (GPay/PhonePe/Paytm)")
        }
      },
      dismissButton = {
        TextButton(onClick = { showDonateDialog = false }) {
          Text("Close")
        }
      }
    )
  }
}

@Composable
fun CategoryCard(category: CategoryItem, onClick: () -> Unit) {
  val title = category.getDisplayTitle()
  val subtitle = category.getDisplaySubtitle()

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
          text = title,
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.Bold,
          color = MaterialTheme.colorScheme.onSurface,
          textAlign = TextAlign.Center
        )
        if (subtitle.isNotBlank()) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
          )
        }
      }
    }
  }
}
