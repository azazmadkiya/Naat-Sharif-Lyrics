package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.NaatViewModel

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val naatViewModel: NaatViewModel = viewModel()
      MyApplicationTheme(darkTheme = naatViewModel.isDarkMode) {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background
        ) {
          NaatNavHost(viewModel = naatViewModel)
        }
      }
    }
  }
}

@Composable
fun NaatNavHost(viewModel: NaatViewModel) {
  val navController = rememberNavController()

  NavHost(navController = navController, startDestination = "splash") {
    composable("splash") {
      SplashScreen(
        onTimeout = {
          val destination = if (viewModel.isLoggedIn) "home" else "auth"
          navController.navigate(destination) {
            popUpTo("splash") { inclusive = true }
          }
        }
      )
    }
    composable("auth") {
      AuthScreen(
        viewModel = viewModel,
        onLoginSuccess = {
          navController.navigate("home") {
            popUpTo("auth") { inclusive = true }
          }
        }
      )
    }
    composable("home") {
      HomeScreen(
        viewModel = viewModel,
        onCategoryClick = { catId, catTitle ->
          navController.navigate("category_list/$catId/$catTitle")
        },
        onNavigate = { route ->
          navController.navigate(route)
        }
      )
    }
    composable(
      route = "category_list/{catId}/{catTitle}",
      arguments = listOf(
        navArgument("catId") { type = NavType.StringType },
        navArgument("catTitle") { type = NavType.StringType }
      )
    ) { backStackEntry ->
      val catId = backStackEntry.arguments?.getString("catId") ?: "naat"
      val catTitle = backStackEntry.arguments?.getString("catTitle") ?: "Naat Sharif"
      CategoryListScreen(
        categoryTitle = catTitle,
        categoryId = catId,
        viewModel = viewModel,
        onNaatClick = {
          navController.navigate("detail")
        },
        onBack = { navController.popBackStack() }
      )
    }
    composable("detail") {
      DetailScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
    composable("search") {
      SearchScreen(
        viewModel = viewModel,
        onNaatClick = {
          navController.navigate("detail")
        },
        onBack = { navController.popBackStack() }
      )
    }
    composable("favorites") {
      FavoritesScreen(
        viewModel = viewModel,
        onNaatClick = {
          navController.navigate("detail")
        },
        onBack = { navController.popBackStack() }
      )
    }
    composable("settings") {
      SettingScreen(
        viewModel = viewModel,
        onNavigateAdmin = {
          navController.navigate("admin_add")
        },
        onNavigatePrivacy = {
          navController.navigate("privacy_policy")
        },
        onLogout = {
          navController.navigate("auth") {
            popUpTo(0) { inclusive = true }
          }
        },
        onBack = { navController.popBackStack() }
      )
    }
    composable("admin_add") {
      AdminAddScreen(
        viewModel = viewModel,
        onAdded = {
          navController.popBackStack()
        },
        onBack = { navController.popBackStack() }
      )
    }
    composable("privacy_policy") {
      PrivacyPolicyScreen(
        viewModel = viewModel,
        onBack = { navController.popBackStack() }
      )
    }
  }
}
