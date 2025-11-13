package com.example.fantasystreams

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    // IMPORTANT: Change this to your server's IP address or domain.
    // If running on a local network, use your computer's local IP (e.g., "192.168.1.5").
    // "127.0.0.1" or "localhost" will NOT work from the Android emulator.
    private val serverUrl = "https://fantasystreams.app" 
    private val client = OkHttpClient()

    private lateinit var leagueIdInput: EditText
    private lateinit var leagueNameInput: EditText
    private lateinit var teamNameInput: EditText
    private lateinit var submitButton: Button
    private lateinit var loadingSpinner: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Get references to the UI elements
        leagueIdInput = findViewById(R.id.league_id_input)
        leagueNameInput = findViewById(R.id.league_name_input)
        teamNameInput = findViewById(R.id.team_name_input)
        submitButton = findViewById(R.id.submit_button)
        loadingSpinner = findViewById(R.id.loading_spinner)

        // Set a click listener on the button
        submitButton.setOnClickListener {
            val leagueId = leagueIdInput.text.toString()
            val leagueName = leagueNameInput.text.toString()
            val teamName = teamNameInput.text.toString()

            if (leagueId.isNotEmpty() && leagueName.isNotEmpty() && teamName.isNotEmpty()) {
                // Show loading spinner and hide button text
                setLoadingState(true)
                // Launch background task to check league
                checkLeagueFile(leagueId, leagueName, teamName)
            } else {
                showToast("Please fill out all fields")
            }
        }
    }

    private fun checkLeagueFile(leagueId: String, leagueName: String, teamName: String) {
        // Use Kotlin Coroutines to move networking off the main (UI) thread
        lifecycleScope.launch(Dispatchers.IO) {
            val requestUrl = "$serverUrl/api/check_league?id=$leagueId&name=$leagueName"
            val request = Request.Builder().url(requestUrl).build()

            try {
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseBody = response.body?.string()
                    val json = JSONObject(responseBody ?: "")

                    if (json.optBoolean("exists", false)) {
                        // File exists! Navigate to new screen
                        // Must run UI code back on the main thread
                        withContext(Dispatchers.Main) {
                            setLoadingState(false)
                            navigateToLeagueHome(leagueName, teamName)
                        }
                    } else {
                        // File doesn't exist
                        withContext(Dispatchers.Main) {
                            setLoadingState(false)
                            // Use the new, specific error string
                            showToast(getString(R.string.league_not_found_error))
                        }
                    }
                }
            } catch (e: Exception) {
                // Handle network errors
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    setLoadingState(false)
                    showToast("Error connecting to server")
                }
            }
        }
    }

    private fun navigateToLeagueHome(leagueName: String, teamName: String) {
        val intent = Intent(this, LeagueHomeActivity::class.java).apply {
            putExtra("LEAGUE_NAME", leagueName)
            putExtra("TEAM_NAME", teamName)
        }
        startActivity(intent)
    }

    private fun setLoadingState(isLoading: Boolean) {
        if (isLoading) {
            loadingSpinner.visibility = View.VISIBLE
            submitButton.text = "" // Hide button text
            submitButton.isEnabled = false
        } else {
            loadingSpinner.visibility = View.GONE
            submitButton.text = getString(R.string.submit_button_text) // Restore text
            submitButton.isEnabled = true
        }
    }

    private fun showToast(message: String) {
        // Use LENGTH_LONG to give the user time to read the longer error message
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }
}
