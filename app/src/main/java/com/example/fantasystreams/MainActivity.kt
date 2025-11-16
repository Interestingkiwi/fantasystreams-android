package com.example.fantasystreams

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var loadingSpinner: ProgressBar
    private lateinit var errorText: TextView
    private lateinit var retryButton: Button

    // This is the new mobile-specific login route
    private val loginUrl by lazy {
        getString(R.string.server_url) + "/mobile_login"
    }
    // This is the page we expect to land on after a successful login
    private val homeUrl by lazy {
        getString(R.string.server_url) + "/home"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // --- [START] DEBUG BYPASS ---
        //
        // Set this to 'true' to skip login and inject a cookie.
        // Set this to 'false' to test the real login flow.
        //
        val IS_DEBUG_BYPASS_ENABLED = true
        val DEBUG_LEAGUE_ID = "22705" // <-- PUT YOUR TEST LEAGUE ID HERE
        val DEBUG_COOKIE_STRING = "session=.eJx9lMnOq0YQRl8l8vrKYjRwpSwAYyYzGX4aE0WIoZkHm2a-Sp49vpGS7LLsr1R1uhZ1fpzSoUdzB8eogfvp-ymrsV3rGs6sHSIOSgEq2tcX8Ri1zuGAn40WkA7gaTWQ6MPDUyoBppIcrel4GKf1BvckuDkl_SkMtCME5h4GTpeST84QQ8r2DOr07T8igukIpw80xi95zuUXjGFhQpJxQsKchDiX4xjGJEQCUwynmJz8NLcwLmYYVdmnjSAYjP43Q6fvv_34n3rUxx08ff9x-iX51BywVU9A06pULk9iaqErqAnBoRhkZdIbv57--OP3b6chnqcyQlM8wZ_zrgIwuLowc8Jlzda-AhZ4HEO4rprM7Qe0x-UwRNPQwP4nKE5TiNA_71MGbhy6xynaCL3eYpLN8vDL3wnaz4ZAJbjxjnHCW9H7eJ1HVEET2-VSNJZNJAfz9rwFwcQ6isVdcHf58iNDCUSc5nehDey75LIh9uKslGI41tuLRElivx_iyVluxSR7t7timj1Fmu9ZtMEEDttkYDJHr3Oqlr63cvdN6SZnSsrDe7-wq2OUhWu7nW3lD_tqRWMdXEUZoSd_5udW3UkXD-lFEOzhnQgScDe_iTcob1DEYYOphKEqTCnxXjbfkrB8zvJMxRba0m5rGD1ZE6bRQBTydlWSR2aIRVbNwjOjNdm8ouQq2i0mNPFj4ngz0inazJ6Jv6600b3VnHJbF_qy7BFmHr_vQoUSK1pBVRuKGm7nEQ_9OM08iffNdTrnz2FccCiuegsag-OVB66RK3txmmM96nNOqnya2iNv1IlDK06kdRQPgC4U3Th1aGrx53WOvRGvD2x7xcAE0xGtzDVN2Wrui4AP8LMUBb00p9tbO6-v0rNwTb6_YkG8-9LVyqOKeRTrZ4_5DZ5clpy9ojNywKiTHunYHb55mrP4JUiAHO47JS-DdZke0eI-ImfFN7wPe0dUA4-88xa5D6LfJvkxoyjUELeQGaoYKY9q_9JCKjYCw94pXXyPbG3y89vRMaHnO09a9VHf2HboV7D3hKQ8dH8yClLyhQRiqg1Xk7mXwQ2NX1hG2476dGZ1OyZi1tTI2rzJIsX311tZ0F3Qe6OJCEWlk8eKzKA--4w3hgrBTiHTnqFiNCtj59LA5HA5SB3VmSOxxdZ5Dz0kH9moNs092KFzxUqtOOuRYScvA4U76xBsQfQ5dpsu8tYHjIEm5LSez_H-WRdufnzzg6V9yR_mgitDNLQXYaEuSWymWw4z7dVKluQeK667b3QOn_AV-D7y5qha1NfaAuCDs-GR6QUJT8IHcqEcTE4Xs0SZzoPeKofZa1lYCG5SU8K8kLKR3b5K6uHpDdkzavCaGaNpDpnq6kw8Ik6YmcMbL1RH2IHFhUjvePb6kQDcXtUIURR_zIYzFxJjPjd5-S-vPi4gLxj27TTC_BOU_wqCV7CYlCsmm4aqewV4Acdg3cjd2GfiTwzD_8TIKKzsmpZVCSiP3t3OE77P6WfZ2-r0Fv_AQJcOPvr84u-Z0bS_foorgfEIx4_S_gKs2ftM.aRZU3A.iim_MXZRUx3vJ-MKhj6Dlqw3no8" // <-- PUT YOUR COOKIE HERE
        //
        // ---

        if (IS_DEBUG_BYPASS_ENABLED) {
            // 1. Get the server URL
            val serverUrl = getString(R.string.server_url) // "https://fantasystreams.app"

            // 2. Manually inject the cookie into the shared CookieManager
            val cookieManager = CookieManager.getInstance()
            cookieManager.setCookie(serverUrl, DEBUG_COOKIE_STRING)
            cookieManager.flush() // Save it

            Log.i("MainActivityBypass", "DEBUG BYPASS: Injected cookie for $serverUrl")

            // 3. Launch LeagueHomeActivity directly
            val intent = Intent(this, LeagueHomeActivity::class.java)
            intent.putExtra("league_id", DEBUG_LEAGUE_ID)
            startActivity(intent)

            // 4. Finish this activity so the user can't "go back" to the login screen
            finish()
            return // IMPORTANT: This skips the rest of the onCreate (like setContentView)
        }

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        webView = findViewById(R.id.webView)
        loadingSpinner = findViewById(R.id.loadingSpinner)
        errorText = findViewById(R.id.errorText)
        retryButton = findViewById(R.id.retryButton)

        // Clear all cookies (like old sessions) for a clean login
        CookieManager.getInstance().removeAllCookies(null)
        CookieManager.getInstance().flush()

        // Configure WebView
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        webView.webChromeClient = WebChromeClient() // Handles alerts, etc.
        webView.webViewClient = object : WebViewClient() {

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                Log.i("MainActivity", "Page finished loading: $url")

                // As soon as the page finishes, check if it's the home URL
                if (url != null && url.startsWith(homeUrl)) {
                    // SUCCESS! We are at the home page.
                    // The server has set the session cookie.
                    // Now we intercept the league_id and league_name.

                    val uri = Uri.parse(url)
                    val leagueId = uri.getQueryParameter("league_id")
                    val leagueName = uri.getQueryParameter("league_name")

                    if (leagueId != null && leagueName != null) {
                        Log.i("MainActivity", "Login success! League ID: $leagueId, Name: $leagueName")
                        // Hide WebView, show spinner (briefly)
                        webView.visibility = View.INVISIBLE
                        loadingSpinner.visibility = View.VISIBLE
                        // Proceed to the main app activity
                        navigateToLeagueHome(leagueId, leagueName)
                    } else {
                        Log.w("MainActivity", "On /home page but missing league params.")
                        showError("Login succeeded but could not get league info. Please retry.")
                    }
                } else {
                    // Not on the home page, so show the WebView
                    loadingSpinner.visibility = View.GONE
                    webView.visibility = View.VISIBLE
                }
            }

            override fun onReceivedError(
                view: WebView?,
                request: WebResourceRequest?,
                error: WebResourceError?
            ) {
                super.onReceivedError(view, request, error)
                // Don't show an error for non-main-frame requests (like failed images)
                if (request?.isForMainFrame == true) {
                    val errorMsg = "Error: ${error?.description}"
                    Log.e("MainActivity", "WebView Error: $errorMsg (Code: ${error?.errorCode})")
                    showError(errorMsg)
                }
            }
        }

        // Set up the retry button
        retryButton.setOnClickListener {
            loadLoginUrl()
        }

        // Start the login process
        loadLoginUrl()
    }

    private fun loadLoginUrl() {
        Log.i("MainActivity", "Loading login URL: $loginUrl")
        showLoading()
        webView.loadUrl(loginUrl)
    }

    private fun navigateToLeagueHome(leagueId: String, leagueName: String) {
        // Save the session cookie. This is critical.
        CookieManager.getInstance().flush()

        val intent = Intent(this, LeagueHomeActivity::class.java).apply {
            putExtra("league_id", leagueId)
            putExtra("league_name", leagueName)
        }
        startActivity(intent)
        finish() // Close the login activity
    }

    private fun showLoading() {
        loadingSpinner.visibility = View.VISIBLE
        webView.visibility = View.INVISIBLE
        errorText.visibility = View.GONE
        retryButton.visibility = View.GONE
    }

    private fun showError(message: String) {
        loadingSpinner.visibility = View.GONE
        webView.visibility = View.INVISIBLE
        errorText.text = message
        errorText.visibility = View.VISIBLE
        retryButton.visibility = View.VISIBLE
    }
}