package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.DatingProfile
import com.example.ui.theme.CoralPrimary
import com.example.ui.theme.GoldVip
import com.example.ui.theme.NopeRed
import com.example.ui.theme.PeachBlush
import com.example.ui.theme.SuperlikeBlue
import com.example.ui.theme.TextPrimaryDark
import com.example.ui.theme.TextSecondaryDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun ProfileDetailBottomSheet(
  profile: DatingProfile,
  isMatched: Boolean = false,
  onLike: () -> Unit = {},
  onPass: () -> Unit = {},
  onSuperLike: () -> Unit = {},
  onBlock: () -> Unit = {},
  onReport: (reason: String, details: String, alsoBlock: Boolean) -> Unit = { _, _, _ -> },
  onDismiss: () -> Unit
) {
  val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
  val isAlreadyMatched = profile.isMutualMatch || isMatched
  val photos = remember(profile.photos) { profile.photos.filter { it.isNotBlank() } }
  val pagerState = rememberPagerState(pageCount = { photos.size.coerceAtLeast(1) })
  val coroutineScope = rememberCoroutineScope()

  var showOptionsMenu by remember { mutableStateOf(false) }
  var showBlockConfirm by remember { mutableStateOf(false) }
  var showReportDialog by remember { mutableStateOf(false) }

  // Clean occupation: remove "at ..." and further values
  val cleanOccupation = remember(profile.occupation) {
    var occ = profile.occupation.trim()
    if (occ.contains(" at ", ignoreCase = true)) {
      occ = occ.substringBefore(" at ").trim()
    }
    occ
  }

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    sheetState = sheetState,
    containerColor = MaterialTheme.colorScheme.background,
    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState())
        .padding(bottom = 32.dp)
        .testTag("profile_detail_bottom_sheet")
    ) {
      // ── 1. Photo Carousel ────────────────────────────────────────────────
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(400.dp)
      ) {
        if (photos.isNotEmpty()) {
          HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
          ) { page ->
            AsyncImage(
              model = ImageRequest.Builder(LocalContext.current)
                .data(photos[page])
                .crossfade(true)
                .build(),
              contentDescription = "${profile.name} photo ${page + 1}",
              modifier = Modifier.fillMaxSize(),
              contentScale = ContentScale.Crop
            )
          }

          // Tap left to go to previous photo, tap right to go to next photo
          if (photos.size > 1) {
            Row(modifier = Modifier.fillMaxSize()) {
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxSize()
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                  ) {
                    if (pagerState.currentPage > 0) {
                      coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                      }
                    }
                  }
              )
              Box(
                modifier = Modifier
                  .weight(1f)
                  .fillMaxSize()
                  .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                  ) {
                    if (pagerState.currentPage < photos.size - 1) {
                      coroutineScope.launch {
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                      }
                    }
                  }
              )
            }
          }
        } else {
          Box(
            modifier = Modifier
              .fillMaxSize()
              .background(
                Brush.verticalGradient(
                  listOf(Color(0xFF2E1A36), Color(0xFF16091D))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Box(
              modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(CoralPrimary.copy(alpha = 0.2f)),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = profile.name.take(1).uppercase().ifBlank { "?" },
                style = MaterialTheme.typography.displaySmall,
                color = CoralPrimary,
                fontWeight = FontWeight.Bold
              )
            }
          }
        }

        // Top Gradient Shadow overlay for story bars and close button
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .align(Alignment.TopCenter)
            .background(
              Brush.verticalGradient(
                listOf(Color.Black.copy(alpha = 0.55f), Color.Transparent)
              )
            )
        )

        // Segmented Story Indicator Bars at the top
        if (photos.size > 1) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 14.dp, vertical = 12.dp)
              .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            photos.indices.forEach { index ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(3.5.dp)
                  .clip(RoundedCornerShape(2.dp))
                  .background(
                    if (index == pagerState.currentPage) Color.White else Color.White.copy(alpha = 0.4f)
                  )
              )
            }
          }
        }

        // Photo Count Badge at Bottom Right
        if (photos.size > 1) {
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.Black.copy(alpha = 0.55f),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(14.dp)
          ) {
            Text(
              text = "${pagerState.currentPage + 1} / ${photos.size}",
              color = Color.White,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }

        // Top Bar Controls: Close Button (Start) and Options Menu ⋮ (End)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(
              top = if (photos.size > 1) 24.dp else 12.dp,
              start = 12.dp,
              end = 12.dp
            )
            .align(Alignment.TopCenter),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Close Button
          IconButton(
            onClick = onDismiss,
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color.Black.copy(alpha = 0.5f))
              .testTag("btn_close_profile_sheet")
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color.White,
              modifier = Modifier.size(20.dp)
            )
          }

          // More Options ⋮ Button
          Box {
            IconButton(
              onClick = { showOptionsMenu = true },
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.5f))
                .testTag("btn_profile_options_menu")
            ) {
              Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "More Options",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }

            DropdownMenu(
              expanded = showOptionsMenu,
              onDismissRequest = { showOptionsMenu = false }
            ) {
              DropdownMenuItem(
                text = { Text("Block ${profile.name}", color = Color(0xFFD32F2F), fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                  Icon(Icons.Default.Block, contentDescription = null, tint = Color(0xFFD32F2F))
                },
                onClick = {
                  showOptionsMenu = false
                  showBlockConfirm = true
                },
                modifier = Modifier.testTag("menu_item_block_profile")
              )
              DropdownMenuItem(
                text = { Text("Report ${profile.name}", color = Color(0xFFE65100), fontWeight = FontWeight.SemiBold) },
                leadingIcon = {
                  Icon(Icons.Default.Flag, contentDescription = null, tint = Color(0xFFE65100))
                },
                onClick = {
                  showOptionsMenu = false
                  showReportDialog = true
                },
                modifier = Modifier.testTag("menu_item_report_profile")
              )
            }
          }
        }
      }

      // ── 2. Profile Details Content ───────────────────────────────────────
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 20.dp, vertical = 18.dp)
      ) {
        // Name, Age & Verified
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = profile.name,
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${profile.age}",
            style = MaterialTheme.typography.headlineLarge.copy(
              fontWeight = FontWeight.Light,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          )
          if (profile.isVerified) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
              imageVector = Icons.Default.Verified,
              contentDescription = "Verified",
              tint = SuperlikeBlue,
              modifier = Modifier.size(22.dp)
            )
          }
          if (profile.isVip) {
            Spacer(modifier = Modifier.width(8.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(GoldVip)
                .padding(horizontal = 7.dp, vertical = 2.dp)
            ) {
              Text(
                text = "👑 VIP",
                color = Color.Black,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold
              )
            }
          }
        }

        // 1. Occupation (Without "at" / further values)
        if (cleanOccupation.isNotBlank()) {
          Spacer(modifier = Modifier.height(10.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Work,
              contentDescription = "Occupation",
              tint = CoralPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = cleanOccupation,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        // 2. Degree / Education below occupation
        if (profile.education.isNotBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.School,
              contentDescription = "Degree",
              tint = CoralPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = profile.education,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.Medium
              )
            )
          }
        }

        // 3. Location / Distance
        if (profile.location.isNotBlank()) {
          Spacer(modifier = Modifier.height(6.dp))
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.LocationOn,
              contentDescription = "Location",
              tint = CoralPrimary,
              modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = profile.location,
              style = MaterialTheme.typography.bodyMedium.copy(
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            )
          }
        }

        Spacer(modifier = Modifier.height(18.dp))

        // Bio section
        if (profile.bio.isNotBlank()) {
          Text(
            text = "About Me",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = profile.bio,
            style = MaterialTheme.typography.bodyMedium.copy(
              color = MaterialTheme.colorScheme.onSurface,
              lineHeight = 22.sp
            )
          )
          Spacer(modifier = Modifier.height(18.dp))
        }

        // Prompt Card (e.g. Quickest way to my heart)
        if (!profile.promptQuestion.isNullOrBlank() && !profile.promptAnswer.isNullOrBlank()) {
          Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
          ) {
            Column(modifier = Modifier.padding(16.dp)) {
              Text(
                text = profile.promptQuestion,
                style = MaterialTheme.typography.labelLarge.copy(
                  color = CoralPrimary,
                  fontWeight = FontWeight.Bold
                )
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = profile.promptAnswer,
                style = MaterialTheme.typography.titleMedium.copy(
                  color = MaterialTheme.colorScheme.onSurface,
                  fontWeight = FontWeight.Medium,
                  lineHeight = 22.sp
                )
              )
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
        }

        // ── 3. Lifestyle & Basics Section (Descriptive with Subheadings) ──
        val lifestyleEntries = remember(profile) {
          val entries = mutableListOf<Pair<String, Pair<String, String>>>() // List of (Subheading, (Emoji, Value))
          if (profile.datingIntention.isNotBlank()) {
            entries.add("Looking For" to ("💘" to profile.datingIntention))
          }
          if (profile.height.isNotBlank()) {
            entries.add("Height" to ("📏" to profile.height))
          }
          if (profile.zodiac.isNotBlank()) {
            val zClean = profile.zodiac.replace(Regex("^[✨♈-♓♌♍♎♏♐♑♒\\s]+"), "").trim().ifBlank { profile.zodiac }
            entries.add("Zodiac Sign" to ("✨" to zClean))
          }
          if (profile.drinking.isNotBlank()) {
            entries.add("Drinking" to ("🍷" to profile.drinking))
          }
          if (profile.smoking.isNotBlank()) {
            entries.add("Smoking" to ("🚭" to profile.smoking))
          }
          if (profile.pets.isNotBlank()) {
            entries.add("Pets" to ("🐾" to profile.pets))
          }
          if (profile.gender.isNotBlank()) {
            entries.add("Gender" to ("👤" to profile.gender))
          }
          entries
        }

        if (lifestyleEntries.isNotEmpty()) {
          Text(
            text = "Lifestyle & Basics",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
          Spacer(modifier = Modifier.height(12.dp))

          // 2-column or structured grid of descriptive detail cards with clear subheadings
          Column(
            verticalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            lifestyleEntries.chunked(2).forEach { rowEntries ->
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                rowEntries.forEach { (subheading, iconAndValue) ->
                  val (emoji, value) = iconAndValue
                  Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                      containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                  ) {
                    Column(
                      modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                      Text(
                        text = subheading,
                        style = MaterialTheme.typography.labelSmall.copy(
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          fontWeight = FontWeight.Medium
                        )
                      )
                      Spacer(modifier = Modifier.height(4.dp))
                      Row(
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Text(
                          text = emoji,
                          fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                          text = value,
                          style = MaterialTheme.typography.bodyMedium.copy(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                          )
                        )
                      }
                    }
                  }
                }
                // If odd number of entries in the last row, place an empty spacer
                if (rowEntries.size == 1) {
                  Spacer(modifier = Modifier.weight(1f))
                }
              }
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
        }

        // ── 4. Passions & Interests Section (BELOW Lifestyle) ───────────────
        if (profile.passions.isNotEmpty()) {
          Text(
            text = "Passions & Interests",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface
            )
          )
          Spacer(modifier = Modifier.height(10.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            profile.passions.forEach { tag ->
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(16.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
                  .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(16.dp))
                  .padding(horizontal = 14.dp, vertical = 7.dp)
              ) {
                Text(
                  text = tag,
                  style = MaterialTheme.typography.labelMedium.copy(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                  )
                )
              }
            }
          }
          Spacer(modifier = Modifier.height(20.dp))
        }

        if (!isAlreadyMatched) {
          // ── 6. Quick Floating Action Row (Pass, Superlike, Like) ────────────
          Spacer(modifier = Modifier.height(12.dp))
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Pass (Close) Button - Icon only, enlarged
            Surface(
              onClick = {
                onPass()
                onDismiss()
              },
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surface,
              shadowElevation = 5.dp,
              border = BorderStroke(1.5.dp, NopeRed.copy(alpha = 0.25f)),
              modifier = Modifier
                .size(62.dp)
                .testTag("inspect_btn_pass")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Pass",
                  tint = NopeRed,
                  modifier = Modifier.size(32.dp)
                )
              }
            }

            // Super Like (Star) Button - Icon only, enlarged
            Surface(
              onClick = {
                onSuperLike()
                onDismiss()
              },
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surface,
              shadowElevation = 5.dp,
              border = BorderStroke(1.5.dp, SuperlikeBlue.copy(alpha = 0.25f)),
              modifier = Modifier
                .size(52.dp)
                .testTag("inspect_btn_superlike")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Star,
                  contentDescription = "Super Like",
                  tint = SuperlikeBlue,
                  modifier = Modifier.size(26.dp)
                )
              }
            }

            // Like (Heart) Button - Icon only, enlarged
            Surface(
              onClick = {
                onLike()
                onDismiss()
              },
              shape = CircleShape,
              color = MaterialTheme.colorScheme.surface,
              shadowElevation = 5.dp,
              border = BorderStroke(1.5.dp, CoralPrimary.copy(alpha = 0.25f)),
              modifier = Modifier
                .size(62.dp)
                .testTag("inspect_btn_like")
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  imageVector = Icons.Default.Favorite,
                  contentDescription = "Like",
                  tint = CoralPrimary,
                  modifier = Modifier.size(32.dp)
                )
              }
            }
          }
        }
      }
    }
  }

  // ── Block Confirmation Dialog ───────────────────────────────────────
  if (showBlockConfirm) {
    AlertDialog(
      onDismissRequest = { showBlockConfirm = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Block,
            contentDescription = null,
            tint = Color(0xFFD32F2F),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Block ${profile.name}?", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "When you block ${profile.name}:",
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))
          Text("• Remove from Discover feed", style = MaterialTheme.typography.bodySmall)
          Text("• Prevent likes between you", style = MaterialTheme.typography.bodySmall)
          Text("• Prevent messaging and chat history", style = MaterialTheme.typography.bodySmall)
          Text("• Prevent future matching", style = MaterialTheme.typography.bodySmall)
          Spacer(modifier = Modifier.height(10.dp))
          Text(
            text = "This action takes effect instantly across the entire platform.",
            style = MaterialTheme.typography.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
          )
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showBlockConfirm = false
            onBlock()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD32F2F)),
          modifier = Modifier.testTag("btn_confirm_block_profile")
        ) {
          Text("Block Profile", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showBlockConfirm = false }) {
          Text("Cancel")
        }
      }
    )
  }

  // ── Report User Dialog ──────────────────────────────────────────────
  if (showReportDialog) {
    val reportReasons = listOf(
      "Fake profile",
      "Spam",
      "Harassment",
      "Inappropriate content",
      "Scam",
      "Other"
    )
    var selectedReason by remember { mutableStateOf(reportReasons.first()) }
    var reportDetails by remember { mutableStateOf("") }
    var alsoBlock by remember { mutableStateOf(true) }

    AlertDialog(
      onDismissRequest = { showReportDialog = false },
      title = {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            imageVector = Icons.Default.Flag,
            contentDescription = null,
            tint = Color(0xFFE65100),
            modifier = Modifier.size(24.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          Text(text = "Report ${profile.name}", fontWeight = FontWeight.Bold)
        }
      },
      text = {
        Column(modifier = Modifier.fillMaxWidth()) {
          Text(
            text = "Select a reason for reporting:",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(8.dp))

          reportReasons.forEach { reason ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .clickable { selectedReason = reason }
                .padding(vertical = 4.dp, horizontal = 4.dp)
                .testTag("report_reason_$reason")
            ) {
              RadioButton(
                selected = selectedReason == reason,
                onClick = { selectedReason = reason },
                colors = RadioButtonDefaults.colors(selectedColor = CoralPrimary)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = reason,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selectedReason == reason) FontWeight.Bold else FontWeight.Normal)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))
          OutlinedTextField(
            value = reportDetails,
            onValueChange = { reportDetails = it },
            placeholder = { Text("Provide details (optional)...", fontSize = 12.sp) },
            label = { Text("Details", fontSize = 12.sp) },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("report_details_input"),
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
              focusedBorderColor = CoralPrimary
            )
          )

          Spacer(modifier = Modifier.height(10.dp))
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(8.dp))
              .clickable { alsoBlock = !alsoBlock }
              .padding(vertical = 4.dp)
              .testTag("report_also_block_checkbox")
          ) {
            Checkbox(
              checked = alsoBlock,
              onCheckedChange = { alsoBlock = it },
              colors = CheckboxDefaults.colors(checkedColor = Color(0xFFD32F2F))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text(
                text = "Also block ${profile.name}",
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = "Hide from Discover & prevent messaging",
                style = MaterialTheme.typography.labelSmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
              )
            }
          }
        }
      },
      confirmButton = {
        Button(
          onClick = {
            showReportDialog = false
            onReport(selectedReason, reportDetails, alsoBlock)
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE65100)),
          modifier = Modifier.testTag("btn_submit_report")
        ) {
          Text("Submit Report", color = Color.White, fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showReportDialog = false }) {
          Text("Cancel")
        }
      }
    )
  }
}

@Composable
fun LifestyleBadge(text: String) {
  Box(
    modifier = Modifier
      .clip(RoundedCornerShape(14.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .border(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
      .padding(horizontal = 13.dp, vertical = 7.dp)
  ) {
    Text(
      text = text,
      style = MaterialTheme.typography.labelMedium.copy(
        color = MaterialTheme.colorScheme.onSurface,
        fontWeight = FontWeight.SemiBold
      )
    )
  }
}
