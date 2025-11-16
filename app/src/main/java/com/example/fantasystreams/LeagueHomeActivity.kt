package com.example.fantasystreams

import android.os.Bundle
import android.util.Log
import android.view.MenuItem // <-- [ADD] Import MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.content.Intent
import android.widget.Toast // <-- [ADD] Import Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle // <-- [ADD] Import
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar // <-- [ADD] Import
import androidx.core.view.GravityCompat // <-- [ADD] Import
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout // <-- [ADD] Import
import com.google.android.material.navigation.NavigationView //
import okhttp3.Call
import okhttp3.Callback
// --- [START] MODIFIED IMPORTS ---
// We need these classes for the custom CookieJar
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
// This is the correct CookieManager used by the WebView
import android.webkit.CookieManager
// --- [END] MODIFIED IMPORTS ---
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException
// --- [REMOVED] java.net.CookieManager ---
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class LeagueHomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

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

    // --- [START] MODIFIED CLIENT ---
    // The OkHttp client, configured with our custom WebViewCookieJar,
    // will now read cookies from the shared android.webkit.CookieManager.
    private val client = OkHttpClient.Builder()
        .cookieJar(WebViewCookieJar())
        .build()
    // --- [END] MODIFIED CLIENT ---

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_league_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.content_root)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            // This now applies padding to the LinearLayout, pushing the Toolbar
            // and content down, out from under the status bar.
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        drawerLayout = findViewById(R.id.main) // This is the DrawerLayout
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

        // Initialize views
        leagueNameText = findViewById(R.id.leagueNameText)
        lastUpdatedText = findViewById(R.id.lastUpdatedText)
        statusMessageText = findViewById(R.id.statusMessageText)
        progressBar = findViewById(R.id.progressBar)
        refreshButton = findViewById(R.id.refreshButton)
        contentContainer = findViewById(R.id.contentContainer)
        updatePromptText = findViewById(R.id.updatePromptText)

        // Get the league ID passed from MainActivity
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
        // Handle navigation view item clicks here.
        when (item.itemId) {
            R.id.nav_league_database -> {
                Toast.makeText(this, "League Database clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_matchup -> {
                Toast.makeText(this, "Matchup clicked", Toast.LENGTH_SHORT).show()
            }
            R.id.nav_lineups -> {
                Toast.makeText(this, "Lineups clicked", Toast.LENGTH_SHORT).show()
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
            R.id.nav_logout -> {
                // 1. Clear all cookies (this logs the user out)
                CookieManager.getInstance().removeAllCookies(null)
                CookieManager.getInstance().flush()

                // 2. Go back to MainActivity (login screen)
                val intent = Intent(this, MainActivity::class.java)

                // 3. Set flags to clear the back-stack
                // This prevents the user from hitting "back" and returning to this activity
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish() // Close this LeagueHomeActivity
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
        // Set UI to loading state
        runOnUiThread {
            progressBar.visibility = View.VISIBLE
            contentContainer.visibility = View.GONE
            statusMessageText.visibility = View.VISIBLE
            statusMessageText.text = getString(R.string.checking_db_status)
            leagueNameText.text = getString(R.string.loading_league)
            lastUpdatedText.text = "..."
        }

        // Get the server URL from strings.xml
        val baseUrl = getString(R.string.server_url)
        val url = "$baseUrl/api/v1/league/$leagueId/database-status"
        Log.i("LeagueHomeActivity", "Fetching URL: $url")

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("LeagueHomeActivity", "Network call failed", e)
                showError("Network error: ${e.message}. Please check your connection.")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                if (!response.isSuccessful) {
                    Log.e("LeagueHomeActivity", "API request failed: ${response.code} $responseBody")
                    // Handle specific error codes
                    when (response.code) {
                        401 -> showError("Not logged in. Please go back and log in again.")
                        403 -> showError("Forbidden. You don't have access to this league.")
                        404 -> {
                            // This is the "exists: false" case
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

                // --- SUCCESS (200 OK) ---
                try {
                    val json = JSONObject(responseBody ?: "{}")
                    Log.d("LeagueHomeActivity", "Received JSON: $json")

                    val leagueName = json.getString("league_name")
                    val lastUpdatedUtc = json.getString("last_updated_utc")

                    // Format the date and update the UI
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
            // Input format from API: "2025-11-13T18:00:00Z" (or similar ISO 8601)
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")

            val date: Date? = try {
                inputFormat.parse(utcDateStr)
            } catch (e: ParseException) {
                // Try with milliseconds if the first format fails
                val inputFormatWithMs = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSSSS'Z'", Locale.US)
                inputFormatWithMs.timeZone = TimeZone.getTimeZone("UTC")
                inputFormatWithMs.parse(utcDateStr)
            }

            // Output format: "Nov 13, 2025 at 1:00 PM"
            val outputFormat = SimpleDateFormat("MMM d, yyyy 'at' h:mm a", Locale.getDefault())
            // Convert to local time zone for display
            outputFormat.timeZone = TimeZone.getDefault()

            if (date != null) {
                outputFormat.format(date)
            } else {
                utcDateStr // Fallback
            }
        } catch (e: Exception) {
            Log.e("LeagueHomeActivity", "Date format error", e)
            utcDateStr // Fallback
        }
    }
}

// --- [START] NEW HELPER CLASS ---
/**
 * A custom OkHttp CookieJar that bridges with the Android WebView's
 * shared CookieManager. This allows OkHttp requests to send the
 * session cookies that were set inside the WebView.
 */
class WebViewCookieJar : CookieJar {

    private val cookieManager = CookieManager.getInstance()

    override fun loadForRequest(url: HttpUrl): List<Cookie> {
        val cookiesString = cookieManager.getCookie(url.toString())
        if (cookiesString != null && cookiesString.isNotEmpty()) {
            // Manually parse the cookie string
            return cookiesString.split(";").mapNotNull { cookieString ->
                Cookie.parse(url, cookieString.trim())
            }
        }
        return emptyList()
    }

    override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
        val urlString = url.toString()
        cookies.forEach { cookie ->
            cookieManager.setCookie(urlString, cookie.toString())
        }
        // Persist the cookies
        cookieManager.flush()
    }
}
// --- [END] NEW HELPER CLASS ---