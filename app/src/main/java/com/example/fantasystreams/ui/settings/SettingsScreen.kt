package com.example.fantasystreams.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.fantasystreams.data.AppPreferences
import com.example.fantasystreams.ui.matchup.ColorTextPrimary
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPreferences
) : ViewModel() {

    data class SettingsState(
        val dataSource: String = "projected",
        val showRawData: Boolean = false
    )

    private val _uiState = MutableStateFlow(SettingsState())
    val uiState = _uiState.asStateFlow()

    init {
        // Load initial values
        _uiState.update {
            it.copy(
                dataSource = prefs.getDataSource(),
                showRawData = prefs.getShowRawData()
            )
        }
    }

    fun onDataSourceSelected(displayName: String) {
        val internalValue = when (displayName) {
            "Season To Date" -> "todate"
            "Combined" -> "combined"
            else -> "projected" // "Projected ROS"
        }
        prefs.saveDataSource(internalValue)
        _uiState.update { it.copy(dataSource = internalValue) }
    }

    fun onShowRawDataChanged(checked: Boolean) {
        prefs.saveShowRawData(checked)
        _uiState.update { it.copy(showRawData = checked) }
    }
}

@Composable
fun SettingsScreen(
    onLogout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val dataSourceOptions = listOf("Projected ROS", "Season To Date", "Combined")

    // Helper to convert internal value back to display string
    val currentDisplayValue = when (uiState.dataSource) {
        "todate" -> "Season To Date"
        "combined" -> "Combined"
        else -> "Projected ROS"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Account Settings",
            style = MaterialTheme.typography.headlineMedium,
            color = ColorTextPrimary,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        // --- Data Source Dropdown ---
        Text(
            text = "Data Source",
            style = MaterialTheme.typography.titleMedium,
            color = ColorTextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        SettingsDropdown(
            options = dataSourceOptions,
            selectedValue = currentDisplayValue,
            onValueSelected = { viewModel.onDataSourceSelected(it) }
        )
        Text(
            text = "Controls which dataset is used for stats and projections throughout the app.",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
        )

        // --- Show Raw Data Toggle ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Show Raw Data",
                    style = MaterialTheme.typography.titleMedium,
                    color = ColorTextPrimary
                )
                Text(
                    text = "Display raw stat numbers instead of fantasy points or ranks where applicable.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
            Switch(
                checked = uiState.showRawData,
                onCheckedChange = { viewModel.onShowRawDataChanged(it) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Logout Button ---
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Text("Logout")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsDropdown(
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