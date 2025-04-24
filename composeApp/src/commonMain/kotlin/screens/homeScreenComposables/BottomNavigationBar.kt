package screens.homeScreenComposables

import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.runtime.Composable
import screens.TabType

@Composable
fun BottomNavigationBar(
    selectedTab: TabType,
    onTabSelected: (TabType) -> Unit,
) {
    BottomNavigation {
        BottomNavigationItem(
            icon = { Icon(Icons.AutoMirrored.Default.List, contentDescription = "Reports") },
            label = { Text("Local") },
            selected = selectedTab == TabType.Local,
            onClick = { onTabSelected(TabType.Local) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.AutoMirrored.Default.List, contentDescription = "Trending") },
            label = { Text("State") },
            selected = selectedTab == TabType.State,
            onClick = { onTabSelected(TabType.State) }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.AutoMirrored.Default.List, contentDescription = "Notifications") },
            label = { Text("National") },
            selected = selectedTab == TabType.National,
            onClick = { onTabSelected(TabType.National) }
        )
    }
}
