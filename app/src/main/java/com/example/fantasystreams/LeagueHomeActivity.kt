// filepath: interestingkiwi/fantasystreams-android/fantasystreams-android-42c52c9aee496104b96dd43261cf4f884eadaadf/app/src/main/java/com/example/fantasystreams/LeagueHomeActivity.kt
package com.example.fantasystreams

import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Intent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import com.example.fantasystreams.ui.matchup.MatchupScreen
import javax.inject.Inject
import android.webkit.CookieManager
import com.example.fantasystreams.ui.settings.SettingsScreen
import com.example.fantasystreams.ui.lineup.LineupScreen
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner

@AndroidEntryPoint
class LeagueHomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    @Inject lateinit var client: OkHttpClient
    private var leagueId: String? = null
    private lateinit var leagueNameText: TextView
    private lateinit var lastUpdatedText: TextView
    private lateinit var statusMessageText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var refreshButton: Button
    private lateinit var contentContainer: LinearLayout
    private lateinit var updatePromptText: TextView
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var toolbar: Toolbar


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_league_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.main)
        navigationView = findViewById(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.nav_open,
            R.string.nav_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        leagueNameText = findViewById(R.id.leagueNameText)
        lastUpdatedText = findViewById(R.id.lastUpdatedText)
        statusMessageText = findViewById(R.id.statusMessageText)
        progressBar = findViewById(R.id.progressBar)
        refreshButton = findViewById(R.id.refreshButton)
        contentContainer = findViewById(R.id.contentContainer)
        updatePromptText = findViewById(R.id.updatePromptText)

        leagueId = intent.getStringExtra("league_id")

        if (leagueId == null) {
            Log.e("LeagueHomeActivity", "No league_id provided in intent")
            showError("No league ID found. Please go back and try again.")
        } else {
            Log.i("LeagueHomeActivity", "Activity started for league_id: $leagueId")
            fetchDatabaseStatus(leagueId!!)
        }

        refreshButton.setOnClickListener {
            leagueId?.let {
                fetchDatabaseStatus(it)
            }
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_league_database -> {
                leagueId?.let { fetchDatabaseStatus(it) }
            }
            R.id.nav_matchup -> {
                leagueNameText.text = "Matchup"
                lastUpdatedText.text = ""
                statusMessageText.visibility = View.GONE
                progressBar.visibility = View.GONE
                contentContainer.removeAllViews()
                contentContainer.visibility = View.VISIBLE

                val composeView = ComposeView(this)
                contentContainer.addView(composeView)

                composeView.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        CompositionLocalProvider(LocalViewModelStoreOwner provides this@LeagueHomeActivity) {
                            MatchupScreen()
                        }
                    }
                }
            }
            R.id.nav_lineups -> {
                leagueNameText.text = "Lineups"
                lastUpdatedText.text = ""
                statusMessageText.visibility = View.GONE
                progressBar.visibility = View.GONE
                contentContainer.removeAllViews()
                contentContainer.visibility = View.VISIBLE

                val composeView = ComposeView(this)
                contentContainer.addView(composeView)

                composeView.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        CompositionLocalProvider(LocalViewModelStoreOwner provides this@LeagueHomeActivity) {
                            LineupScreen()
                        }
                    }
                }
            }
            R.id.nav_free_agents -> {
                Toast.makeText(this, "Free Agents clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_goalie_planning -> {
                Toast.makeText(this, "Goalie Planning clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_trade_helper -> {
                Toast.makeText(this, "Trade Helper clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_schedule_insights -> {
                Toast.makeText(this, "Schedule Insights clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_season_history -> {
                Toast.makeText(this, "Season History clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_settings -> {
                leagueNameText.text = "Settings"
                lastUpdatedText.text = ""
                statusMessageText.visibility = View.GONE
                progressBar.visibility = View.GONE
                contentContainer.removeAllViews()
                contentContainer.visibility = View.VISIBLE

                val composeView = ComposeView(this)
                contentContainer.addView(composeView)

                composeView.apply {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    setContent {
                        CompositionLocalProvider(LocalViewModelStoreOwner provides this@LeagueHomeActivity) {
                            SettingsScreen(
                                onLogout = {
                                    CookieManager.getInstance().removeAllCookies(null)
                                    CookieManager.getInstance().flush()
                                    val intent = Intent(context, MainActivity::class.java)
                                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                    startActivity(intent)
                                    finish()
                                }
                            )
                        }
                    }
                }
            }
        }

        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    private fun fetchDatabaseStatus(leagueId: String) {
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            contentContainer.visibility = View.GONE
            statusMessageText.visibility = View.VISIBLE
            statusMessageText.text = getString(R.string.checking_db_status)
            leagueNameText.text = getString(R.string.loading_league)
            lastUpdatedText.text = "..."
        }

        val baseUrl = getString(R.string.server_url)
        val url = "$baseUrl/api/v1/league/$leagueId/database-status"
        Log.i("LeagueHomeActivity", "Fetching URL: $url")

        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LeagueHomeActivity", "Network call failed", e)
                showError("Network error: ${e.message}. Please check your connection.")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e("LeagueHomeActivity", "API request failed: ${response.code} $responseBody")
                    when (response.code) {
                        401 -> showError("Not logged in. Please go back and log in again.")
                        403 -> showError("Forbidden. You don't have access to this league.")
                        404 -> {
                            try {
                                val json = JSONObject(responseBody ?: "{}")
                                val message = json.optString("message", "Database not found.")
                                runOnUiThread {
                                    progressBar.visibility = View.GONE
                                    statusMessageText.visibility = View.VISIBLE
                                    leagueNameText.text = getString(R.string.no_database_found)
                                    statusMessageText.text = message
                                }
                            } catch (e: Exception) {
                                showError("Database not found, but error parsing response.")
                            }
                        }
                        else -> showError("Error ${response.code}: Could not fetch league status.")
                    }
                    return
                }
                try {
                    val json = JSONObject(responseBody ?: "{}")
                    Log.d("LeagueHomeActivity", "Received JSON: $json")
                    val leagueName = json.getString("league_name")
                    val lastUpdatedUtc = json.getString("last_updated_utc")
                    val formattedDate = formatUtcTimestamp(lastUpdatedUtc)
                    runOnUiThread {
                        progressBar.visibility = View.GONE
                        statusMessageText.visibility = View.GONE
                        contentContainer.visibility = View.VISIBLE
                        leagueNameText.text = leagueName
                        lastUpdatedText.text = getString(R.string.last_updated_prefix, formattedDate)
                    }

                } catch (e: Exception) {
                    Log.e("LeagueHomeActivity", "Failed to parse JSON", e)
                    showError("Error: Could not parse server response.")
                }
            }
        })
    }

    private fun showError(message: String) {
        runOnUiThread {
            progressBar.visibility = View.GONE
            contentContainer.visibility = View.GONE
            statusMessageText.visibility = View.VISIBLE
            statusMessageText.text = message
            leagueNameText.text = getString(R.string.error)
            lastUpdatedText.text = ""
        }
    }

    private fun formatUtcTimestamp(utcDateStr: String): String {
        return try {
            val inputFormatWithMs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX", Locale.US)
            val inputFormatNoMs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", Locale.US)
            val date: Date? = try {
                inputFormatWithMs.parse(utcDateStr)
            } catch (e: ParseException) {
                inputFormatNoMs.parse(utcDateStr)
            }
            val outputFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            outputFormat.timeZone = TimeZone.getDefault()
            if (date != null) outputFormat.format(date) else utcDateStr
        } catch (e: Exception) {
            Log.e("LeagueHomeActivity", "Date format error for input: $utcDateStr", e)
            utcDateStr
        }
    }
}