package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun AuthScreen(viewModel: NaatViewModel, onLoginSuccess: () -> Unit) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isSignUp by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var resetMessage by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Naat Sharif Lyrics - Authentication") },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.primary,
          titleContentColor = Color.White
        )
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(24.dp),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = if (isSignUp) "Create Account" else "Welcome Back",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )
      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Access Naat, Kalam, and Hamd Sharif collection",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(24.dp))

      OutlinedTextField(
        value = email,
        onValueChange = { email = it },
        label = { Text("Email Address") },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )
      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = password,
        onValueChange = { password = it },
        label = { Text("Password") },
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
            val isAdminRole = email.contains("admin", ignoreCase = true)
            if (isSignUp) {
              viewModel.signUp(email, password, isAdminRole) { success ->
                isLoading = false
                if (success) onLoginSuccess()
              }
            } else {
              viewModel.signIn(email, password, isAdminRole) { success ->
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
          Text(if (isSignUp) "Sign Up" else "Login", fontSize = 16.dp.value.sp)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      TextButton(onClick = { 
        isSignUp = !isSignUp
        resetMessage = null
      }) {
        Text(if (isSignUp) "Already have an account? Login" else "Don't have an account? Sign Up")
      }
    }
  }
}
