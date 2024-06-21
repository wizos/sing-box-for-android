package io.mo.viaport.ui.element

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(navController: NavController, bottomNavItems: List<BottomNavItem>){
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val titleMap by remember { derivedStateOf { bottomNavItems.associateBy { it.route } }}
    titleMap[currentRoute]?.let {
        TopAppBar(
            title = {
                Text(text = it.label)
            },
            // navigationIcon = {
            //     IconButton(onClick = { navController.popBackStack() }) {
            //         Icon(Icons.AutoMirrored.Filled.NavigateBefore, "Back")
            //     }
            // },
            // scrollBehavior = scrollBehavior,
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.background,
                scrolledContainerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
            )
        )
    }
}