package com.farukayata.yemektarifi.data.remote.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState


    @Composable
    fun BottomNavigationBar(navController: NavController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        NavigationBar {
            NavigationBarItem(
                icon = { Icon(Icons.Default.Home, contentDescription = "Ana Sayfa") },
                label = { Text("Ana Sayfa") },
                selected = currentRoute == "home",
                onClick = {
                    if (currentRoute != "home") {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Favorite, contentDescription = "Favoriler") },
                label = { Text("Favoriler") },
                selected = currentRoute == "favorites",
                onClick = {
                    if (currentRoute != "favorites") {
                        navController.navigate("favorites") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
            NavigationBarItem(
                icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                label = { Text("Profil") },
                selected = currentRoute == "profile",
                onClick = {
                    if (currentRoute != "profile") {
                        navController.navigate("profile") {
                            popUpTo(navController.graph.startDestinationId)
                            launchSingleTop = true
                        }
                    }
                }
            )
        }
    }
