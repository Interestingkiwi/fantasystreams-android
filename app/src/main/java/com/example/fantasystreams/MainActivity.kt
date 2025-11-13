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