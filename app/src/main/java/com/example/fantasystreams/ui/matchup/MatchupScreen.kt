// filepath: interestingkiwi/fantasystreams-android/fantasystreams-android-42c52c9aee496104b96dd43261cf4f884eadaadf/app/src/main/java/com/example/fantasystreams/ui/matchup/MatchupScreen.kt
package com.example.fantasystreams.ui.matchup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fantasystreams.data.model.MatchupStatsResponse
import com.example.fantasystreams.data.model.ScoringCategory

// Define custom colors to match your dark theme/pastel logic
val ColorWin = Color(0xFF1B5E20) // Dark Green
val ColorLoss = Color(0xFFB71C1C) // Dark Red
val ColorTie = Color(0xFFF9A825) // Dark Yellow/Gold
val ColorTextPrimary = Color.White
val ColorTextSecondary = Color.LightGray

@Composable
fun MatchupScreen(
    viewModel: MatchupViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // FIX: Refresh data when this screen enters composition.
    // This ensures we pick up any Data Source changes from Settings.
    LaunchedEffect(Unit) {
        viewModel.fetchMatchupStats()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        if (uiState.isLoadingPage) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (uiState.errorMessage != null && uiState.pageData == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = "Error loading page data: ${uiState.errorMessage}",
                    color = MaterialTheme.colorScheme.error
                )
            }
        } else if (uiState.pageData != null) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // --- Dropdown Controls ---
                item {
                    Column {
                        DropdownSelector(
                            label = "Week",
                            options = uiState.pageData!!.weeks.map { "Week ${it.weekNum} (${it.startDate})" },
                            selectedValue = uiState.pageData!!.weeks
                                .find { it.weekNum.toString() == uiState.selectedWeek }
                                ?.let { "Week ${it.weekNum} (${it.startDate})" } ?: "Select Week",
                            onValueSelected = { selectedString ->
                                val weekNum = selectedString.split(" ")[1]
                                viewModel.onWeekSelected(weekNum)
                            }
                        )
                        Spacer(Modifier.height(8.dp))
                        DropdownSelector(
                            label = "Your Team",
                            options = uiState.pageData!!.teams.map { it.name },
                            selectedValue = uiState.selectedTeam ?: "Select Team",
                            onValueSelected = { teamName -> viewModel.onTeamSelected(teamName) }
                        )
                        Spacer(Modifier.height(8.dp))
                        DropdownSelector(
                            label = "Opponent",
                            options = uiState.pageData!!.teams
                                .filter { it.name != uiState.selectedTeam }
                                .map { it.name },
                            selectedValue = uiState.selectedOpponent ?: "Select Opponent",
                            onValueSelected = { teamName -> viewModel.onOpponentSelected(teamName) }
                        )
                    }
                }

                // --- Stats Display ---
                item {
                    if (uiState.isLoadingStats) {
                        Box(modifier = Modifier.fillMaxWidth().padding(32.dp), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    } else if (uiState.errorMessage != null) {
                        Text(text = "Error: ${uiState.errorMessage}", color = MaterialTheme.colorScheme.error)
                    } else if (uiState.matchupStats != null) {
                        val stats = uiState.matchupStats!!

                        // 1. Matchup Stats Table
                        Text("Matchup Stats", style = MaterialTheme.typography.titleMedium, color = ColorTextPrimary)
                        MatchupStatsTable(
                            stats = stats,
                            categories = uiState.pageData!!.scoringCategories,
                            team1Name = uiState.selectedTeam ?: "Team 1",
                            team2Name = uiState.selectedOpponent ?: "Team 2"
                        )

                        Spacer(Modifier.height(16.dp))

                        // 2. Game Counts Table
                        Text("Game Counts", style = MaterialTheme.typography.titleMedium, color = ColorTextPrimary)
                        GameCountsTable(
                            gameCounts = stats.gameCounts,
                            team1Name = uiState.selectedTeam ?: "Team 1",
                            team2Name = uiState.selectedOpponent ?: "Team 2"
                        )

                        Spacer(Modifier.height(16.dp))

                        // 3. Unused Roster Spots Table
                        Text("Unused Roster Spots", style = MaterialTheme.typography.titleMedium, color = ColorTextPrimary)
                        if (stats.team1UnusedSpots != null) {
                            UnusedRosterSpotsTable(unusedSpots = stats.team1UnusedSpots)
                        } else {
                            Text("No unused spots data.", color = ColorTextSecondary)
                        }
                    }
                }
            }
        }
    }
}

// ... (Rest of the file: MatchupStatsTable, GameCountsTable, etc. remain the same)
@Composable
fun MatchupStatsTable(
    stats: MatchupStatsResponse,
    categories: List<ScoringCategory>,
    team1Name: String,
    team2Name: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
            .background(Color(0xFF212121))
    ) {
        Row(
            modifier = Modifier
                .background(Color(0xFF424242))
                .padding(8.dp)
        ) {
            Text("Cat", Modifier.weight(1f), fontWeight = FontWeight.Bold, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            Text("T1 Live", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            Text("T1 Proj", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            Text("T2 Live", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            Text("T2 Proj", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
        }

        val hiddenCats = listOf("GA", "TOI/G", "SA", "SV")
        val visibleCats = categories.filter { !hiddenCats.contains(it.category) }

        visibleCats.forEach { cat ->
            val category = cat.category
            val t1Live = stats.team1Stats.live[category] ?: 0.0
            val t1Row = stats.team1Stats.row[category] ?: 0.0
            val t2Live = stats.team2Stats.live[category] ?: 0.0
            val t2Row = stats.team2Stats.row[category] ?: 0.0

            fun fmt(valNum: Double): String {
                return if (category == "SVpct") "%.3f".format(valNum)
                else if (category == "GAA") "%.2f".format(valNum)
                else if (valNum % 1.0 == 0.0) valNum.toInt().toString()
                else "%.1f".format(valNum)
            }

            val isGaa = category == "GAA"

            fun getColor(val1: Double, val2: Double): Color {
                if (val1 == val2) return ColorTie
                if (isGaa) {
                    return if (val1 < val2) ColorWin else ColorLoss
                }
                return if (val1 > val2) ColorWin else ColorLoss
            }

            val t1LiveColor = getColor(t1Live, t2Live)
            val t2LiveColor = getColor(t2Live, t1Live)
            val t1RowColor = getColor(t1Row, t2Row)
            val t2RowColor = getColor(t2Row, t1Row)

            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(category, Modifier.weight(1f), fontWeight = FontWeight.Bold, color = ColorTextSecondary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Text(fmt(t1Live), Modifier.weight(1f).background(t1LiveColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(2.dp), textAlign = TextAlign.Center, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Spacer(Modifier.width(2.dp))
                Text(fmt(t1Row), Modifier.weight(1f).background(t1RowColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(2.dp), textAlign = TextAlign.Center, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Spacer(Modifier.width(2.dp))
                Text(fmt(t2Live), Modifier.weight(1f).background(t2LiveColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(2.dp), textAlign = TextAlign.Center, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
                Spacer(Modifier.width(2.dp))
                Text(fmt(t2Row), Modifier.weight(1f).background(t2RowColor.copy(alpha = 0.3f), RoundedCornerShape(4.dp)).padding(2.dp), textAlign = TextAlign.Center, color = ColorTextPrimary, fontSize = MaterialTheme.typography.bodySmall.fontSize)
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.DarkGray)
        }
    }
}

@Composable
fun GameCountsTable(
    gameCounts: com.example.fantasystreams.data.model.GameCounts,
    team1Name: String,
    team2Name: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
            .background(Color(0xFF212121))
    ) {
        Row(Modifier.background(Color(0xFF424242)).padding(8.dp)) {
            Text("Team", Modifier.weight(1.5f), fontWeight = FontWeight.Bold, color = ColorTextPrimary)
            Text("Total", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
            Text("Rem", Modifier.weight(1f), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = ColorTextPrimary)
        }

        Row(Modifier.padding(8.dp)) {
            Text(team1Name, Modifier.weight(1.5f), color = ColorTextPrimary, maxLines = 1)
            Text(gameCounts.team1Total.toString(), Modifier.weight(1f), textAlign = TextAlign.Center, color = ColorTextPrimary)
            Text(gameCounts.team1Remaining.toString(), Modifier.weight(1f), textAlign = TextAlign.Center, color = ColorTextPrimary)
        }
        HorizontalDivider(thickness = 0.5.dp, color = Color.DarkGray)

        Row(Modifier.padding(8.dp)) {
            Text(team2Name, Modifier.weight(1.5f), color = ColorTextPrimary, maxLines = 1)
            Text(gameCounts.team2Total.toString(), Modifier.weight(1f), textAlign = TextAlign.Center, color = ColorTextPrimary)
            Text(gameCounts.team2Remaining.toString(), Modifier.weight(1f), textAlign = TextAlign.Center, color = ColorTextPrimary)
        }
    }
}

@Composable
fun UnusedRosterSpotsTable(unusedSpots: Map<String, Map<String, String>>) {
    val positions = listOf("C", "LW", "RW", "D", "G")
    val sortedDays = unusedSpots.keys.sorted()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.DarkGray, RoundedCornerShape(4.dp))
            .background(Color(0xFF212121))
    ) {
        Row(Modifier.background(Color(0xFF424242)).padding(8.dp)) {
            Text("Day", Modifier.weight(1f), color = ColorTextPrimary, fontWeight = FontWeight.Bold)
            positions.forEach { pos ->
                Text(pos, Modifier.weight(1f), color = ColorTextPrimary, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
            }
        }

        sortedDays.forEach { day ->
            val spotsForDay = unusedSpots[day] ?: emptyMap()
            Row(Modifier.padding(8.dp)) {
                Text(day, Modifier.weight(1f), color = ColorTextPrimary, fontWeight = FontWeight.Medium)
                positions.forEach { pos ->
                    val value = spotsForDay[pos] ?: "-"
                    val isPositive = try {
                        value.replace("*", "").toInt() > 0
                    } catch(e: Exception) { false }

                    val textColor = if (isPositive) ColorWin else ColorTextSecondary
                    val fontWeight = if (isPositive) FontWeight.Bold else FontWeight.Normal

                    Text(
                        text = value,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        color = textColor,
                        fontWeight = fontWeight
                    )
                }
            }
            HorizontalDivider(thickness = 0.5.dp, color = Color.DarkGray)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownSelector(
    label: String,
    options: List<String>,
    selectedValue: String,
    onValueSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        OutlinedTextField(
            value = selectedValue,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        onValueSelected(selectionOption)
                        expanded = false
                    }
                )
            }
        }
    }
}