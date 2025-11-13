package com.example.fantasystreams

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LeagueHomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_league_home)

        // Get the data passed from MainActivity
        val leagueName = intent.getStringExtra("LEAGUE_NAME") ?: "League"
        val teamName = intent.getStringExtra("TEAM_NAME") ?: "User"

        // Find the TextViews
        val leagueTitleText = findViewById<TextView>(R.id.league_title_text)
        val welcomeText = findViewById<TextView>(R.id.welcome_text)

        // Set the text
        leagueTitleText.text = "$leagueName Home"
        welcomeText.text = getString(R.string.welcome_message, teamName)
    }
}
