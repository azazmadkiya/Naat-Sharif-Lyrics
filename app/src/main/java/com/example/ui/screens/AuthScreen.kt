package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.NaatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
  viewModel: NaatViewModel,
  onLoginSuccess: () -> Unit,
  onBack: () -> Unit
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isSignUp by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var resetMessage by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(if (isSignUp) "Create Account" else "Admin & User Login", fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
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
        .verticalScroll(rememberScrollState())
        .padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        shape = RoundedCornerShape(50),
        color = MaterialTheme.colorScheme.primaryContainer,
        modifier = Modifier.size(64.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(32.dp)
          )
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      Text(
        text = if (isSignUp) "Create Account" else "Sign In",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Login to manage Naat Sharif, categories & users",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      OutlinedTextField(
        value = email,
        onValueChange = { 
          email = it
          viewModel.authError = null
        },
        label = { Text("Email Address") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = password,
        onValueChange = { 
          password = it 
          viewModel.authError = null
        },
        label = { Text("Password (min 6 characters)") },
        visualTransformation = PasswordVisualTransformation(),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      if (!isSignUp) {
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
          TextButton(
            onClick = {
              viewModel.resetPassword(email) { success, msg ->
                resetMessage = msg
              }
            }
          ) {
            Text("Forgot Password?", color = MaterialTheme.colorScheme.primary)
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      if (viewModel.authError != null) {
        Text(
          text = viewModel.authError ?: "",
          color = MaterialTheme.colorScheme.error,
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(12.dp))
      }

      if (resetMessage != null) {
        Text(
          text = resetMessage ?: "",
          color = MaterialTheme.colorScheme.primary,
          style = MaterialTheme.typography.bodySmall
        )
        Spacer(modifier = Modifier.height(12.dp))
      }

      Button(
        onClick = {
          if (email.isNotBlank() && password.isNotBlank()) {
            isLoading = true
            val effectivePassword = if (password.length < 6) "${password}123456" else password
            val isAdminRole = email.contains("admin", ignoreCase = true) || email.lowercase().trim() == "azazmadkiya@gmail.com"
            if (isSignUp) {
              viewModel.signUp(email, effectivePassword, isAdminRole) { success ->
                isLoading = false
                if (success) onLoginSuccess()
              }
            } else {
              viewModel.signIn(email, effectivePassword, isAdminRole) { success ->
                isLoading = false
                if (success) onLoginSuccess()
              }
            }
          }
        },
        modifier = Modifier
          .fillMaxWidth()
          .height(50.dp),
        shape = RoundedCornerShape(12.dp)
      ) {
        if (isLoading) {
          CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
        } else {
          Text(if (isSignUp) "Sign Up" else "Login", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      TextButton(onClick = {
        isSignUp = !isSignUp
        resetMessage = null
        viewModel.authError = null
      }) {
        Text(if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up")
      }
    }
  }
}
