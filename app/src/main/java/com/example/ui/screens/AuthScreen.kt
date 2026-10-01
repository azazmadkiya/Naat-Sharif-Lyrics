package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.NaatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
  viewModel: NaatViewModel,
  onLoginSuccess: () -> Unit
) {
  var email by remember { mutableStateOf("") }
  var password by remember { mutableStateOf("") }
  var isPasswordVisible by remember { mutableStateOf(false) }
  var isSignUp by remember { mutableStateOf(false) }
  var isLoading by remember { mutableStateOf(false) }
  var resetMessage by remember { mutableStateOf<String?>(null) }
  var localError by remember { mutableStateOf<String?>(null) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            if (isSignUp) "Create Account" else "Sign In",
            fontWeight = FontWeight.Bold
          )
        },
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
        text = if (isSignUp) "Create Account" else "Welcome Back",
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(6.dp))

      Text(
        text = "Sign in or register to access Naat Sharif & Kalam collection",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(24.dp))

      OutlinedTextField(
        value = email,
        onValueChange = { 
          email = it
          localError = null
          viewModel.authError = null
        },
        label = { Text("Email Address") },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      Spacer(modifier = Modifier.height(16.dp))

      OutlinedTextField(
        value = password,
        onValueChange = { 
          password = it 
          localError = null
          viewModel.authError = null
        },
        label = { Text("Password (min 6 characters)") },
        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        trailingIcon = {
          IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
            Icon(
              imageVector = if (isPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
              contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
            )
          }
        },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        singleLine = true
      )

      if (!isSignUp) {
        Spacer(modifier = Modifier.height(4.dp))
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
          TextButton(
            onClick = {
              if (email.isBlank()) {
                localError = "Please enter your email address to reset password."
                return@TextButton
              }
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

      val displayError = localError ?: viewModel.authError
      if (displayError != null) {
        Surface(
          color = MaterialTheme.colorScheme.errorContainer,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = displayError,
            color = MaterialTheme.colorScheme.onErrorContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      if (resetMessage != null) {
        Surface(
          color = MaterialTheme.colorScheme.primaryContainer,
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = resetMessage ?: "",
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(12.dp)
          )
        }
        Spacer(modifier = Modifier.height(12.dp))
      }

      Button(
        onClick = {
          val cleanEmail = email.trim()
          val cleanPass = password.trim()

          if (cleanEmail.isBlank()) {
            localError = "Please enter your email address."
            return@Button
          }
          if (cleanPass.length < 6) {
            localError = "Password must be at least 6 characters long."
            return@Button
          }

          isLoading = true
          localError = null
          viewModel.authError = null
          val isAdminRole = cleanEmail.contains("admin", ignoreCase = true) || cleanEmail.lowercase() == "azazmadkiya@gmail.com"

          if (isSignUp) {
            viewModel.signUp(cleanEmail, cleanPass, isAdminRole) { success ->
              isLoading = false
              if (success) onLoginSuccess()
            }
          } else {
            viewModel.signIn(cleanEmail, cleanPass, isAdminRole) { success ->
              isLoading = false
              if (success) onLoginSuccess()
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
          Text(if (isSignUp) "Sign Up" else "Sign In", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      TextButton(onClick = {
        isSignUp = !isSignUp
        resetMessage = null
        localError = null
        viewModel.authError = null
      }) {
        Text(if (isSignUp) "Already registered? Sign In" else "New user? Create an Account")
      }
    }
  }
}
