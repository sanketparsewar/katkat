package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocalFireDepartment
import androidx.compose.material.icons.outlined.PersonOutline
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark

enum class KatkatTab(
  val title: String,
  val selectedIcon: ImageVector,
  val unselectedIcon: ImageVector,
  val testTag: String
) {
  DISCOVER("Discover", Icons.Filled.LocalFireDepartment, Icons.Outlined.LocalFireDepartment, "tab_discover"),
  LIKES_YOU("Likes You", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder, "tab_likes_you"),
  MATCHES("Matches", Icons.Filled.ChatBubble, Icons.Outlined.ChatBubbleOutline, "tab_matches"),
  PROFILE("Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline, "tab_profile")
}

@Composable
fun KatkatBottomNav(
  currentTab: KatkatTab,
  onTabSelected: (KatkatTab) -> Unit,
  likesCount: Int = 0,
  unreadMatchesCount: Int = 0,
  modifier: Modifier = Modifier
) {
  NavigationBar(
    modifier = modifier
      .fillMaxWidth()
      .navigationBarsPadding(),
    containerColor = MaterialTheme.colorScheme.surface,
    tonalElevation = 8.dp
  ) {
    KatkatTab.values().forEach { tab ->
      val isSelected = currentTab == tab
      NavigationBarItem(
        selected = isSelected,
        onClick = { onTabSelected(tab) },
        icon = {
          BadgedBox(
            badge = {
              if (tab == KatkatTab.LIKES_YOU && likesCount > 0) {
                Badge(
                  containerColor = CoralPrimary,
                  contentColor = Color.White
                ) {
                  Text(text = "$likesCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              } else if (tab == KatkatTab.MATCHES && unreadMatchesCount > 0) {
                Badge(
                  containerColor = CoralPrimary,
                  contentColor = Color.White
                ) {
                  Text(text = "$unreadMatchesCount", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          ) {
            Icon(
              imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
              contentDescription = tab.title,
              modifier = Modifier.size(24.dp)
            )
          }
        },
        label = {
          Text(
            text = tab.title,
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
              fontSize = 11.sp
            )
          )
        },
        colors = NavigationBarItemDefaults.colors(
          selectedIconColor = CoralPrimary,
          selectedTextColor = CoralPrimary,
          unselectedIconColor = TextSecondaryDark,
          unselectedTextColor = TextSecondaryDark,
          indicatorColor = PeachBlush
        ),
        modifier = Modifier.testTag(tab.testTag)
      )
    }
  }
}
