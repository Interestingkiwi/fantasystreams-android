// filepath: interestingkiwi/fantasystreams-android/fantasystreams-android-42c52c9aee496104b96dd43261cf4f884eadaadf/app/src/main/java/com/example/fantasystreams/ui/lineup/LineupScreen.kt
package com.example.fantasystreams.ui.lineup

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fantasystreams.data.model.LineupPlayer
import com.example.fantasystreams.data.model.OpponentStat
import com.example.fantasystreams.ui.matchup.* // Reuse colors/components

@Composable
fun LineupScreen(
    viewModel: LineupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { viewModel.refreshData() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (uiState.pageData != null) {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. Selectors
                item {
                    Column {
                        DropdownSelector(
                            label = "Week",
                            options = uiState.pageData!!.weeks.map { "Week ${it.weekNum} (${it.startDate})" },
                            selectedValue = uiState.pageData!!.weeks.find { it.weekNum.toString() == uiState.selectedWeek }?.let { "Week ${it.weekNum} (${it.startDate})" } ?: "",
                            onValueSelected = { viewModel.onWeekSelected(it.split(" ")[1]) }
                        )
                        Spacer(Modifier.height(8.dp))
                        DropdownSelector(
                            label = "Team",
                            options = uiState.pageData!!.teams.map { it.name },
                            selectedValue = uiState.selectedTeam ?: "",
                            onValueSelected = { viewModel.onTeamSelected(it) }
                        )
                    }
                }

                // 2. Category Drawer
                item {
                    Button(
                        onClick = { viewModel.toggleCategoryDrawer() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF424242))
                    ) {
                        Text("Prioritize Categories")
                        Icon(if (uiState.isCategoryDrawerOpen) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown, contentDescription = null)
                    }
                    AnimatedVisibility(visible = uiState.isCategoryDrawerOpen) {
                        CategoryFilterDrawer(
                            allCategories = uiState.rosterData?.scoringCategories ?: emptyList(),
                            enabledCategories = uiState.enabledCategories,
                            onToggle = { viewModel.toggleCategory(it) }
                        )
                    }
                }

                // 3. Roster Table
                item {
                    if (uiState.rosterData != null) {
                        Text("Roster & Projections", style = MaterialTheme.typography.titleMedium, color = ColorTextPrimary)
                        Spacer(Modifier.height(8.dp))
                        RosterTable(
                            players = uiState.rosterData!!.players,
                            categories = uiState.rosterData!!.scoringCategories,
                            showRawData = uiState.showRawData
                        )
                    }
                }

                // 4. Unused Spots
                item {
                    if (uiState.rosterData?.unusedRosterSpots != null) {
                        Text("Unused Roster Spots", style = MaterialTheme.typography.titleMedium, color = ColorTextPrimary)
                        UnusedRosterSpotsTable(unusedSpots = uiState.rosterData!!.unusedRosterSpots!!)
                    }
                }

                // 5. Daily Lineup
                item {
                    if (uiState.rosterData != null && uiState.dayOptions.isNotEmpty()) {
                        Text("Daily Lineup", style = MaterialTheme.typography.titleMedium, color = ColorTextPrimary)
                        Spacer(Modifier.height(8.dp))
                        DropdownSelector(
                            label = "Select Date",
                            options = uiState.dayOptions,
                            selectedValue = uiState.selectedDate ?: "",
                            onValueSelected = { viewModel.onDateSelected(it) }
                        )

                        val lineup = uiState.rosterData!!.dailyOptimalLineups[uiState.selectedDate]
                        if (lineup != null) {
                            DailyLineupView(lineup = lineup)
                        } else {
                            Text("No lineup data for this date.", color = ColorTextSecondary)
                        }
                    }
                }

                item { Spacer(Modifier.height(40.dp)) }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CategoryFilterDrawer(
    allCategories: List<String>,
    enabledCategories: Set<String>,
    onToggle: (String) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth().padding(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        allCategories.forEach { cat ->
            FilterChip(
                selected = enabledCategories.contains(cat),
                onClick = { onToggle(cat) },
                label = { Text(cat) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = ColorWin,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

@Composable
fun RosterTable(
    players: List<LineupPlayer>,
    categories: List<String>,
    showRawData: Boolean
) {
    // Horizontal Scroll Container
    Column(modifier = Modifier.horizontalScroll(rememberScrollState())) {
        // Header
        Row(modifier = Modifier.background(Color(0xFF424242)).padding(8.dp)) {
            HeaderCell("Player", 120.dp)
            HeaderCell("This Wk", 80.dp) // M T W
            HeaderCell("Next Wk", 80.dp) // M T W
            HeaderCell("PP Util", 60.dp)
            HeaderCell("Rank", 60.dp)
            categories.forEach { cat -> HeaderCell(cat, 50.dp) }
        }

        // Rows
        players.forEach { player ->
            Row(
                modifier = Modifier
                    .background(Color(0xFF212121))
                    .border(width = 0.5.dp, color = Color.DarkGray)
                    .padding(vertical = 8.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Player Name (Modal: Team/Pos)
                PlayerNameCell(player, 120.dp)

                // This Week (Modal: Opponents)
                OpponentsCell(player, 80.dp)

                // Next Week (No Modal)
                ScheduleCell(player.gamesNextWeek, 80.dp) {}

                // PP Util (Modal)
                PPCell(player, 60.dp)

                // Total Rank (Modal: Inverse)
                RankCell(player, 60.dp, showRawData)

                // Stats
                categories.forEach { cat ->
                    val rawVal = player.getStatValue(cat)
                    val rankVal = player.getRankValue(cat)

                    // Logic: If settings=ShowRawData -> Show Raw. Else -> Show Rank.
                    val displayVal = if (showRawData) rawVal else rankVal

                    // Formatting
                    val txt = if (displayVal == null) "-"
                    else if (displayVal % 1.0 == 0.0) displayVal.toInt().toString()
                    else "%.1f".format(displayVal)

                    Text(
                        text = txt,
                        modifier = Modifier.width(50.dp),
                        textAlign = TextAlign.Center,
                        color = ColorTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun OpponentsCell(player: LineupPlayer, width: Dp) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        OpponentsDialog(player = player, onDismiss = { showDialog = false })
    }
    ScheduleCell(player.gamesThisWeek, width) { showDialog = true }
}

@Composable
fun PPCell(player: LineupPlayer, width: Dp) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        PPUtilDialog(player = player, onDismiss = { showDialog = false })
    }
    ClickableValueCell(
        value = "${player.ppTimePct?.toInt() ?: 0}%",
        width = width,
        onClick = { showDialog = true }
    )
}

@Composable
fun RankCell(player: LineupPlayer, width: Dp, showRawData: Boolean) {
    var showDialog by remember { mutableStateOf(false) }
    if (showDialog) {
        RankDialog(player = player, showingRawCurrently = showRawData, onDismiss = { showDialog = false })
    }
    ClickableValueCell(
        value = player.totalRank?.toString() ?: "-",
        width = width,
        isBold = true,
        color = if ((player.totalRank ?: 0.0) > 0) ColorWin else ColorTextPrimary,
        onClick = { showDialog = true }
    )
}

@Composable
fun HeaderCell(text: String, width: Dp) {
    Text(
        text = text,
        modifier = Modifier.width(width),
        color = ColorTextPrimary,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
}

@Composable
fun PlayerNameCell(player: LineupPlayer, width: Dp) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        InfoDialog(title = player.playerName, onDismiss = { showDialog = false }) {
            Text("Team: ${player.team ?: "N/A"}", color = ColorTextPrimary)
            Text("Positions: ${player.eligiblePositions ?: "N/A"}", color = ColorTextPrimary)
            Text("Status: ${player.status ?: "Active"}", color = ColorTextSecondary)
        }
    }

    Text(
        text = player.playerName,
        modifier = Modifier.width(width).clickable { showDialog = true },
        color = Color(0xFF64B5F6), // Link color
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        fontSize = 12.sp
    )
}

@Composable
fun ScheduleCell(games: List<String>?, width: Dp, onClick: () -> Unit) {
    // Convert "Mon", "Tue" -> "M", "T"
    val text = games?.joinToString(" ") { it.take(1) } ?: "-"
    Text(
        text = text,
        modifier = Modifier.width(width).clickable { onClick() },
        color = ColorTextPrimary,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
}

@Composable
fun ClickableValueCell(value: String, width: Dp, isBold: Boolean = false, color: Color = ColorTextPrimary, onClick: () -> Unit) {
    Text(
        text = value,
        modifier = Modifier.width(width).clickable { onClick() },
        color = color,
        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
        textAlign = TextAlign.Center,
        fontSize = 12.sp
    )
}

// --- Dialogs ---

@Composable
fun InfoDialog(title: String, onDismiss: () -> Unit, content: @Composable () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF424242))) {
            Column(Modifier.padding(16.dp)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = ColorTextPrimary)
                Spacer(Modifier.height(8.dp))
                content()
                Spacer(Modifier.height(16.dp))
                Button(onClick = onDismiss, modifier = Modifier.align(Alignment.End)) { Text("Close") }
            }
        }
    }
}

@Composable
fun OpponentsDialog(player: LineupPlayer, onDismiss: () -> Unit) {
    InfoDialog(title = "Opponents for ${player.playerName}", onDismiss = onDismiss) {
        player.opponentStats?.forEach { stat ->
            Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${stat.gameDate} vs ${stat.opponent}", color = ColorTextPrimary)
                Column(horizontalAlignment = Alignment.End) {
                    Text("GA/G: ${stat.gaGm ?: "-"}", color = ColorTextSecondary, fontSize = 10.sp)
                    Text("PK%: ${stat.pkPct ?: "-"}", color = ColorTextSecondary, fontSize = 10.sp)
                }
            }
            HorizontalDivider(color = Color.Gray)
        } ?: Text("No games found.", color = ColorTextSecondary)
    }
}

@Composable
fun PPUtilDialog(player: LineupPlayer, onDismiss: () -> Unit) {
    InfoDialog(title = "PP Stats: ${player.playerName}", onDismiss = onDismiss) {
        Text("PP Time %: ${player.ppTimePct?.toInt() ?: 0}%", color = ColorTextPrimary)
        Text("PP Goals: ${player.ppGoals ?: 0}", color = ColorTextPrimary)
        Text("PP Assists: ${player.ppAssists ?: 0}", color = ColorTextPrimary)
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        Text("League PP Goals: ${player.lgPpGoals ?: 0}", color = ColorTextSecondary)
        Text("League PP Assists: ${player.lgPpAssists ?: 0}", color = ColorTextSecondary)
    }
}

@Composable
fun RankDialog(player: LineupPlayer, showingRawCurrently: Boolean, onDismiss: () -> Unit) {
    val title = if (showingRawCurrently) "Category Ranks" else "Raw Stats"
    InfoDialog(title = title, onDismiss = onDismiss) {
        // Show the inverse of what is on the table
        val value = if (showingRawCurrently) player.totalCatRank else player.totalRank
        Text("Total Value: $value", color = ColorTextPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun DailyLineupView(lineup: Map<String, List<LineupPlayer>>) {
    // Ordered positions
    val order = listOf("C", "LW", "RW", "D", "G", "BN")

    Column(Modifier.background(Color(0xFF212121), RoundedCornerShape(8.dp)).padding(8.dp)) {
        order.forEach { pos ->
            val players = lineup[pos] ?: emptyList()
            if (players.isNotEmpty()) {
                Row(Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                    Text(pos, Modifier.width(40.dp), fontWeight = FontWeight.Bold, color = ColorWin)
                    Column {
                        players.forEach { p ->
                            Text(p.playerName, color = ColorTextPrimary)
                        }
                    }
                }
                HorizontalDivider(color = Color.DarkGray)
            }
        }
    }
}